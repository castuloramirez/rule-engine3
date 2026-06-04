package com.ruleengine.legacy.engine;

import com.ruleengine.legacy.model.LegacyEngineResult;
import com.ruleengine.legacy.model.LegacyRuleProcess;
import com.ruleengine.model.EmailPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LegacyRuleEngineRunner — top-level orchestrator.
 *
 * Mirrors the initEngine() → getProccess() pipeline in RuleEngine.java.
 *
 * Flow:
 *   1. Set defaults (Category=General, Status=Open, Client=UNIQA)
 *   2. For each enabled process → evaluate all steps in order
 *   3. Steps that fail stop their process (DecisionException)
 *   4. All processes run independently (one stop doesn't block others)
 *   5. Collect final category, status, column values into LegacyEngineResult
 */
@Slf4j
@Component
public class LegacyRuleEngineRunner {

    /**
     * Run all processes against an email payload.
     *
     * @param payload   the incoming email
     * @param processes the configured processes (from YAML or programmatic config)
     * @return          LegacyEngineResult with category, status, log etc.
     */
    public LegacyEngineResult run(EmailPayload payload, List<LegacyRuleProcess> processes) {

        log.info("=== LegacyRuleEngine START [{}] ===", payload.getMessageId());

        // ---- Init context (mirrors initEngine defaults) ----
        LegacyEngineContext context = new LegacyEngineContext();
        context.setCurrentEmailCategory("General");
        context.setStatusMvp("Open");
        context.setClient("UNIQA");
        context.addLog("StartingREv3.0.2");

        MVPMemory        memory    = new MVPMemory();
        LegacyEngineResult result  = LegacyEngineResult.builder().build();
        LegacyProcessEvaluator evaluator = new LegacyProcessEvaluator(context, memory);

        String body    = nullSafe(payload.getBodyPlain());
        String subject = nullSafe(payload.getSubject());
        String from    = nullSafe(payload.getFrom());
        String to      = nullSafe(payload.getTo());
        String cc      = "";   // EmailPayload can be extended with CC if needed
        String bcc     = "";

        // ---- Run each enabled process ----
        for (LegacyRuleProcess process : processes) {
            if (!process.isEnabled()) continue;

            log.info("[Process:{}] evaluating", process.getProcessName());
            try {
                evaluator.evaluate(process, body, subject, from, to, cc, bcc, result);
            } catch (Exception e) {
                log.error("[Process:{}] unexpected error: {}", process.getProcessName(), e.getMessage(), e);
                result.getLog().add("ERROR[" + process.getProcessName() + "]:" + e.getMessage());
            }
        }

        // ---- Finalize result ----
        result.setCategory(context.getCurrentEmailCategory());
        result.setStatus(context.getStatusMvp());
        result.setClient(context.getClient());
        result.setClaimNumber(context.getClaimNumber());
        result.setPolicyNumber(context.getPolicyNumber());
        result.setBranch(context.getBranch());
        result.setColumnUpdates(context.getColumnUpdates());
        result.setLog(context.getLog());

        // Add memory dump to log for debugging
        result.getLog().add("=== Memory Dump ===");
        result.getLog().add(memory.dump());
        result.getLog().add("FinishingREv3.0.2");

        log.info("=== LegacyRuleEngine END. Category={} Status={} PassedSteps={} ===",
                result.getCategory(), result.getStatus(), result.getPassedSteps().size());

        return result;
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}
