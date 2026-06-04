package com.ruleengine.controller;

import com.ruleengine.legacy.engine.LegacyRuleEngineRunner;
import com.ruleengine.legacy.model.LegacyEngineResult;
import com.ruleengine.legacy.model.LegacyRuleProcess;
import com.ruleengine.model.EmailPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/legacy")
@RequiredArgsConstructor
public class LegacyRuleEngineController {

    private final LegacyRuleEngineRunner  legacyRunner;
    private final List<LegacyRuleProcess> legacyProcesses;

    @PostMapping("/evaluate")
    public ResponseEntity<LegacyEngineResult> evaluate(
            @RequestBody EmailPayload payload) {

        if (payload.getMessageId() == null || payload.getMessageId().isBlank()) {
            payload.setMessageId("legacy-" + System.currentTimeMillis());
        }

        log.info("[LegacyController] evaluate() messageId={}", payload.getMessageId());
        LegacyEngineResult result = legacyRunner.run(payload, legacyProcesses);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test")
    public ResponseEntity<LegacyEngineResult> test(
            @RequestParam(defaultValue = "invoice") String type) {

        EmailPayload sample = buildSample(type);
        log.info("[LegacyController] test() type={}", type);
        LegacyEngineResult result = legacyRunner.run(sample, legacyProcesses);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/processes")
    public ResponseEntity<List<Map<String, Object>>> listProcesses() {
        List<Map<String, Object>> summary = legacyProcesses.stream()
                .map(p -> Map.<String, Object>of(
                        "processId",   p.getProcessId(),
                        "processName", p.getProcessName(),
                        "enabled",     p.isEnabled(),
                        "branching",   p.isBranching(),
                        "stepCount",   p.getSteps().size(),
                        "steps",       p.getSteps().stream()
                                .map(s -> Map.of(
                                        "id",           s.getId(),
                                        "name",         s.getName(),
                                        "type",         s.getType(),
                                        "searchMethod", s.getSearchMethod() != null ? s.getSearchMethod() : "",
                                        "condition",    s.getCondition()    != null ? s.getCondition()    : "",
                                        "searchTerm",   s.getSearchTerm()   != null ? s.getSearchTerm()   : "",
                                        "searchIn",     s.getSearchIn()     != null ? s.getSearchIn()     : ""
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        long enabled  = legacyProcesses.stream().filter(LegacyRuleProcess::isEnabled).count();
        long disabled = legacyProcesses.stream().filter(p -> !p.isEnabled()).count();

        return ResponseEntity.ok(Map.of(
            "status",             "UP",
            "engine",             "LegacyRuleEngine v3.0.2 (standalone)",
            "totalProcesses",     legacyProcesses.size(),
            "enabledProcesses",   enabled,
            "disabledProcesses",  disabled,
            "rulesSource",        "rules-legacy.yml (replaces Dataverse)",
            "supportedTypes",     List.of("SearchWord", "Regex", "RegexFirstMatch",
                                          "Set", "Get", "If", "Declaration",
                                          "SetVariable", "Operation", "Case",
                                          "InclusionWords", "ExclusionWords"),
            "endpoints", Map.of(
                "evaluate",  "POST /api/legacy/evaluate",
                "test",      "POST /api/legacy/test?type=invoice|support|spam|urgent|newsletter|policy",
                "processes", "GET  /api/legacy/processes",
                "status",    "GET  /api/legacy/status"
            )
        ));
    }

    // -----------------------------------------------------------------------
    // Sample payloads for /test — all constructors fixed with 10 args
    // -----------------------------------------------------------------------
    private EmailPayload buildSample(String type) {
        return switch (type.toLowerCase()) {
            case "support" -> new EmailPayload(
                "customer@company.com",
                "support@yourapp.com",
                "Help needed: Cannot login to my account — error 403",
                "Hi support team, I'm experiencing an issue logging in. I get error 403 Forbidden. My account ID is 98765. This is a critical problem for my business. Please help ASAP.",
                null,
                "2026-03-13T10:00:00Z",
                "<legacy-support-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",
                0.0
            );
            case "spam" -> new EmailPayload(
                "promo@megadeals.com",
                "you@gmail.com",
                "LIMITED TIME: 80% OFF all products — special offer today only!",
                "Don't miss this promotional offer! Huge discounts available. Click here to shop. To unsubscribe from these emails click here.",
                null,
                "2026-03-13T10:00:00Z",
                "<legacy-spam-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",
                0.0
            );
            case "urgent" -> new EmailPayload(
                "monitoring@alerts.com",
                "you@gmail.com",
                "CRITICAL: Action required — Production server unreachable",
                "URGENT: Your production API server has been down for 5 minutes. Emergency response needed immediately. All users affected.",
                null,
                "2026-03-13T10:00:00Z",
                "<legacy-urgent-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",
                0.0
            );
            case "newsletter" -> new EmailPayload(
                "news@techweekly.com",
                "you@gmail.com",
                "Tech Weekly Digest — March 13 2026",
                "Welcome to your weekly digest. Top stories this week: AI advances and open source roundup. You are receiving this because you subscribed to our newsletter.",
                null,
                "2026-03-13T10:00:00Z",
                "<legacy-newsletter-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",
                0.0
            );
            case "policy" -> new EmailPayload(
                "client@insurance.com",
                "you@gmail.com",
                "Policy renewal notice — Policy 5500002597",
                "Dear customer your policy number 5500002597 is due for renewal. Claim number 207-2-13740-22 has been registered. Please review the attached documents.",
                null,
                "2026-03-13T10:00:00Z",
                "<legacy-policy-" + System.currentTimeMillis() + "@test.com>",
                List.of("https://s3.amazonaws.com/policy-renewal.pdf"),
                "UNKNOWN",
                0.0
            );
            case "domain" -> new EmailPayload(
                "agent@support.com",
                "you@gmail.com",
                "New ticket submitted from support domain",
                "A customer has submitted a new support ticket. Please review and assign accordingly.",
                null,
                "2026-03-13T10:00:00Z",
                "<legacy-domain-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",
                0.0
            );
            default -> new EmailPayload(
                "billing@acmecorp.com",
                "you@gmail.com",
                "Invoice #1042 — Payment required by March 30",
                "Dear customer please find your invoice for services rendered in March 2026. Amount: $1250.00. Due date: March 30 2026. Reference: INV-1042.",
                null,
                "2026-03-13T09:23:14Z",
                "<legacy-invoice-" + System.currentTimeMillis() + "@test.com>",
                List.of("https://s3.amazonaws.com/invoice-1042.pdf"),
                "UNKNOWN",
                0.0
            );
        };
    }
}
