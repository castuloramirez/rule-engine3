package com.ruleengine.controller;

import com.ruleengine.model.*;
import com.ruleengine.service.RuleEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RuleEngineController — REST API
 *
 * Endpoints:
 *   POST /api/rules/evaluate   — main webhook (Zapier posts here)
 *   GET  /api/rules/emails     — list all processed emails (Postman testing)
 *   GET  /api/rules/emails/{id}— get one email by ID
 *   GET  /api/rules/status     — health/config status
 *   POST /api/rules/test       — test with a sample email (no auth required)
 */
@Slf4j
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleEngineController {

    private final RuleEngineService  ruleEngineService;
    private final EmailRepository    emailRepository;

    // -----------------------------------------------------------------------
    // POST /api/rules/evaluate
    // Main webhook — Zapier posts Gmail data here
    // Header: X-Zapier-Secret: your-secret (set in application.properties)
    // -----------------------------------------------------------------------
    @PostMapping("/evaluate")
    public ResponseEntity<RuleEngineResponse> evaluate(
            @RequestHeader(value = "X-Zapier-Secret", required = false) String secret,
            @RequestBody EmailPayload payload) {

        // Validate shared secret (skip if not configured)
        String configuredSecret = System.getenv("ZAPIER_SECRET");
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            if (!configuredSecret.equals(secret)) {
                log.warn("Rejected request — invalid X-Zapier-Secret");
                return ResponseEntity.status(401).build();
            }
        }

        if (payload.getMessageId() == null || payload.getMessageId().isBlank()) {
            payload.setMessageId("manual-" + System.currentTimeMillis());
        }

        RuleEngineResponse response = ruleEngineService.evaluate(payload);
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------------
    // POST /api/rules/test
    // Convenience endpoint for Postman — sends a pre-built sample email
    // -----------------------------------------------------------------------
    @PostMapping("/test")
    public ResponseEntity<RuleEngineResponse> test(
            @RequestParam(defaultValue = "invoice") String type) {

        EmailPayload sample = buildSamplePayload(type);
        RuleEngineResponse response = ruleEngineService.evaluate(sample);
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------------
    // GET /api/rules/emails
    // List all emails stored in H2
    // -----------------------------------------------------------------------
    @GetMapping("/emails")
    public ResponseEntity<List<EmailRecord>> listEmails(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        List<EmailRecord> records;
        if (category != null) {
            records = emailRepository.findByAiCategory(category.toUpperCase());
        } else if (status != null) {
            records = emailRepository.findByStatus(status);
        } else {
            records = emailRepository.findAll();
        }
        return ResponseEntity.ok(records);
    }

    // -----------------------------------------------------------------------
    // GET /api/rules/emails/{id}
    // Get one email by database ID
    // -----------------------------------------------------------------------
    @GetMapping("/emails/{id}")
    public ResponseEntity<EmailRecord> getEmail(@PathVariable Long id) {
        return emailRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------------------------
    // GET /api/rules/status
    // Returns current configuration
    // -----------------------------------------------------------------------
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "status",        "UP",
            "totalEmails",   emailRepository.count(),
            "description",   "Gmail Rule Engine with AI Classification",
            "aiProviders",   List.of("keyword-fallback (default)", "ollama (local)", "huggingface (free API)"),
            "endpoints",     Map.of(
                "evaluate",  "POST /api/rules/evaluate",
                "test",      "POST /api/rules/test?type=invoice|support|spam|attachment|urgent",
                "emails",    "GET  /api/rules/emails",
                "h2console", "GET  /h2-console"
            )
        ));
    }

    // -----------------------------------------------------------------------
    // Sample payloads for /test endpoint (UPDATED FOR AI FIELDS)
    // -----------------------------------------------------------------------
    private EmailPayload buildSamplePayload(String type) {
        return switch (type.toLowerCase()) {
            case "support" -> new EmailPayload(
                "john@company.com",
                "you@gmail.com",
                "Help needed: login issue with my account",
                "Hi, I'm unable to log into my account since yesterday. Getting error code 403. Please help!",
                "<p>Hi, I'm unable to log in...</p>",
                "2026-03-13T10:00:00Z",
                "<support-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",      // aiCategory
                0.0             // aiConfidence
            );
            case "spam" -> new EmailPayload(
                "promo@deals.com",
                "you@gmail.com",
                "🎉 HUGE DISCOUNT — 80% off today only!",
                "Don't miss this deal! Click here to unsubscribe if you don't want more offers.",
                "<p>HUGE DISCOUNT...</p>",
                "2026-03-13T10:00:00Z",
                "<spam-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",
                0.0
            );
            case "attachment" -> new EmailPayload(
                "colleague@work.com",
                "you@gmail.com",
                "Q1 Report — please review",
                "Hi, please find attached the Q1 report for your review.",
                "<p>Hi, please find attached...</p>",
                "2026-03-13T10:00:00Z",
                "<attachment-" + System.currentTimeMillis() + "@test.com>",
                List.of("https://s3.amazonaws.com/q1-report.pdf", "https://s3.amazonaws.com/q1-data.xlsx"),
                "UNKNOWN",
                0.0
            );
            case "urgent" -> new EmailPayload(
                "ceo@company.com",
                "you@gmail.com",
                "URGENT: Server down — production critical issue",
                "Our production server is down. Customers are affected. Need immediate action!",
                "<p>URGENT: Server down...</p>",
                "2026-03-13T10:00:00Z",
                "<urgent-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",
                0.0
            );
            case "newsletter" -> new EmailPayload(
                "newsletter@techweekly.com",
                "you@gmail.com",
                "Tech Weekly Digest — March 13, 2026",
                "This week in tech: AI news, open source roundup. You're receiving this because you subscribed.",
                "<p>This week in tech...</p>",
                "2026-03-13T10:00:00Z",
                "<newsletter-" + System.currentTimeMillis() + "@test.com>",
                List.of(),
                "UNKNOWN",
                0.0
            );
            default -> new EmailPayload( // invoice
                "billing@acmecorp.com",
                "you@gmail.com",
                "Invoice #1042 from Acme Corp — March 2026",
                "Hi, please find your invoice for March 2026. Amount due: $1,250.00. Due date: March 30.",
                "<p>Hi, please find your invoice...</p>",
                "2026-03-13T10:00:00Z",
                "<invoice-" + System.currentTimeMillis() + "@test.com>",
                List.of("https://s3.amazonaws.com/invoice-1042.pdf"),
                "UNKNOWN",
                0.0
            );
        };
    }
}
