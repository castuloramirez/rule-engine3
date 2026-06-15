package com.ruleengine.service;

import com.ruleengine.ai.AIClassifierService;
import com.ruleengine.model.*;
import com.ruleengine.rules.EmailRule;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * RuleEngineService — main orchestrator.
 *
 * Pipeline:
 *   1. AI classifies the email (Ollama / HuggingFace / keyword-fallback)
 *   2. Rules are evaluated in priority order
 *   3. All matching rules fire their actions
 *   4. Result is persisted to H2
 *   5. Response is returned to caller (Zapier)
 */

@Service
public class RuleEngineService {

    private final static Logger log = LogManager.getLogger(RuleEngineService.class);
    private final AIClassifierService        aiClassifier;
    private final List<EmailRule>            registeredRules;
    private final EmailRepository            emailRepository;

    public RuleEngineService(AIClassifierService aiClassifier, List<EmailRule> registeredRules, EmailRepository emailRepository) {
        this.aiClassifier = aiClassifier;
        this.registeredRules = registeredRules;
        this.emailRepository = emailRepository;
    }

    public RuleEngineResponse evaluate(EmailPayload payload) {
        log.info("=== Processing email [{}] from {} ===",
                payload.getMessageId(), payload.getFrom());

        // ----------------------------------------------------------------
        // Step 1: AI Classification
        // ----------------------------------------------------------------
        AIClassificationResult aiResult = aiClassifier.classify(payload);
        payload.setAiCategory(aiResult.category());
        payload.setAiConfidence(aiResult.confidence());

        log.info("AI classified as: {} (confidence: {}, model: {})",
                aiResult.category(), aiResult.confidence(), aiResult.modelUsed());

        // ----------------------------------------------------------------
        // Step 2: Evaluate rules in priority order (skip FallbackRule if others matched)
        // ----------------------------------------------------------------
        ActionTracker tracker = new ActionTracker();
        List<EmailRule> sorted = registeredRules.stream()
                .sorted(Comparator.comparingInt(EmailRule::priority))
                .toList();

        for (EmailRule rule : sorted) {
            // Skip FallbackRule if any real rule already matched
            if (rule.priority() == 99 && tracker.hasMatches()) {
                log.debug("Skipping FallbackRule — {} rules already matched", tracker.getMatchedRules().size());
                continue;
            }

            try {
                if (rule.evaluate(payload)) {
                    log.info("Rule [{}] MATCHED — executing actions", rule.name());
                    rule.execute(payload, tracker);
                } else {
                    log.debug("Rule [{}] did not match", rule.name());
                }
            } catch (Exception e) {
                log.error("Rule [{}] threw exception: {}", rule.name(), e.getMessage(), e);
            }
        }

        // ----------------------------------------------------------------
        // Step 3: Build response
        // ----------------------------------------------------------------
        RuleEngineResponse response = RuleEngineResponse.builder()
                .messageId(payload.getMessageId())
                .matchedRules(tracker.getMatchedRules())
                .actionsExecuted(tracker.getActionsExecuted())
                .status(tracker.hasMatches() ? "processed" : "no_match")
                .aiCategory(aiResult.category())
                .aiConfidence(aiResult.confidence())
                .aiModel(aiResult.modelUsed())
                .build();

        // ----------------------------------------------------------------
        // Step 4: Persist to H2
        // ----------------------------------------------------------------
        try {
            emailRepository.save(new EmailRecord(payload, response));
            log.info("Email record saved to DB");
        } catch (Exception e) {
            log.warn("Failed to save email record: {}", e.getMessage());
        }

        log.info("=== Done. Matched rules: {} | Actions: {} ===",
                tracker.getMatchedRules(), tracker.getActionsExecuted());

        return response;
    }
}
