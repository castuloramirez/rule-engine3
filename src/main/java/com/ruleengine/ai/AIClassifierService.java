package com.ruleengine.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruleengine.model.AIClassificationResult;
import com.ruleengine.model.EmailPayload;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * AI Classifier Service — classifies emails using FREE AI options:
 *
 * Option A: Ollama (local, 100% free)
 *   - Install: https://ollama.ai
 *   - Run:     ollama run llama3
 *   - Set:     ai.provider=ollama in application.properties
 *
 * Option B: Hugging Face Inference API (free tier, no credit card)
 *   - Sign up: https://huggingface.co
 *   - Get token from: https://huggingface.co/settings/tokens
 *   - Set:     ai.provider=huggingface, ai.huggingface.token=hf_xxx
 *
 * Option C: Keyword fallback (zero dependencies, always works)
 *   - Set:     ai.provider=keyword
 *   - Uses regex/keyword matching — good enough for demos
 */

@Service
public class AIClassifierService {

    private static final Logger log = LogManager.getLogger(AIClassifierService.class);

    @Value("${ai.provider:keyword}")
    private String aiProvider;

    @Value("${ai.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ai.ollama.model:llama3}")
    private String ollamaModel;

    @Value("${ai.huggingface.token:}")
    private String huggingFaceToken;

    @Value("${ai.huggingface.model:facebook/bart-large-mnli}")
    private String huggingFaceModel;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    public AIClassificationResult classify(EmailPayload payload) {
        log.info("Classifying email [{}] using provider: {}", payload.getMessageId(), aiProvider);

        try {
            return switch (aiProvider.toLowerCase()) {
                case "ollama"       -> classifyWithOllama(payload);
                case "huggingface"  -> classifyWithHuggingFace(payload);
                default             -> classifyWithKeywords(payload);
            };
        } catch (Exception e) {
            log.warn("AI classification failed ({}), falling back to keywords: {}", aiProvider, e.getMessage());
            return classifyWithKeywords(payload);
        }
    }

    // -----------------------------------------------------------------------
    // Option A: Ollama — local LLM (llama3, mistral, gemma, etc.)
    // -----------------------------------------------------------------------

    private AIClassificationResult classifyWithOllama(EmailPayload payload) throws IOException {
        String prompt = buildClassificationPrompt(payload);

        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of(
                "model",  ollamaModel,
                "prompt", prompt,
                "stream", false
            )
        );

        Request request = new Request.Builder()
                .url(ollamaUrl + "/api/generate")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama returned HTTP " + response.code());
            }
            String body = response.body().string();
            JsonNode json = objectMapper.readTree(body);
            String rawResponse = json.path("response").asText().trim().toUpperCase();

            String category = extractCategory(rawResponse);
            log.info("Ollama classified email as: {} (raw: {})", category, rawResponse);

            return AIClassificationResult.builder()
                    .category(category)
                    .confidence(0.9)
                    .modelUsed("ollama-" + ollamaModel)
                    .rawResponse(rawResponse)
                    .build();
        }
    }

    // -----------------------------------------------------------------------
    // Option B: Hugging Face Inference API — zero-shot classification
    //   Free tier: ~30,000 requests/month
    //   Model: facebook/bart-large-mnli (zero-shot, no fine-tuning needed)
    // -----------------------------------------------------------------------

    private AIClassificationResult classifyWithHuggingFace(EmailPayload payload) throws IOException {
        String text = buildEmailSummary(payload);

        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of(
                "inputs", text,
                "parameters", java.util.Map.of(
                    "candidate_labels", new String[]{
                        "invoice", "customer support", "spam", "attachment",
                        "newsletter", "urgent", "other"
                    }
                )
            )
        );

        Request request = new Request.Builder()
                .url("https://api-inference.huggingface.co/models/" + huggingFaceModel)
                .addHeader("Authorization", "Bearer " + huggingFaceToken)
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HuggingFace returned HTTP " + response.code());
            }
            String body = response.body().string();
            JsonNode json = objectMapper.readTree(body);

            // HuggingFace returns labels sorted by score descending
            String topLabel  = json.path("labels").get(0).asText().toUpperCase();
            double topScore  = json.path("scores").get(0).asDouble();

            // Map HF label → our categories
            String category = mapHuggingFaceLabel(topLabel);
            log.info("HuggingFace classified email as: {} (score: {})", category, topScore);

            return AIClassificationResult.builder()
                    .category(category)
                    .confidence(topScore)
                    .modelUsed("huggingface-" + huggingFaceModel)
                    .rawResponse(topLabel + "=" + topScore)
                    .build();
        }
    }

    // -----------------------------------------------------------------------
    // Option C: Keyword fallback — no network, no API key, always works
    // -----------------------------------------------------------------------

    private AIClassificationResult classifyWithKeywords(EmailPayload payload) {
        String subject = nullSafe(payload.getSubject()).toLowerCase();
        String body    = nullSafe(payload.getBodyPlain()).toLowerCase();
        String from    = nullSafe(payload.getFrom()).toLowerCase();
        String combined = subject + " " + body + " " + from;

        String category;
        double confidence;

        if (matchesAny(combined, "invoice", "billing", "payment", "receipt", "order", "purchase")) {
            category   = "INVOICE";
            confidence = 0.85;
        } else if (matchesAny(combined, "support", "ticket", "help", "issue", "problem", "bug", "error")) {
            category   = "SUPPORT";
            confidence = 0.80;
        } else if (matchesAny(combined, "unsubscribe", "opt-out", "spam", "promotion", "marketing")) {
            category   = "SPAM";
            confidence = 0.75;
        } else if (!nullSafe(payload.getAttachments() != null ?
                payload.getAttachments().toString() : "").isEmpty()
                && payload.getAttachments() != null
                && !payload.getAttachments().isEmpty()) {
            category   = "ATTACHMENT";
            confidence = 0.90;
        } else if (matchesAny(combined, "newsletter", "weekly", "digest", "roundup", "update")) {
            category   = "NEWSLETTER";
            confidence = 0.70;
        } else if (matchesAny(combined, "urgent", "asap", "immediately", "critical", "emergency")) {
            category   = "URGENT";
            confidence = 0.75;
        } else {
            category   = "OTHER";
            confidence = 0.50;
        }

        log.info("Keyword fallback classified email as: {} (confidence: {})", category, confidence);

        return AIClassificationResult.builder()
                .category(category)
                .confidence(confidence)
                .modelUsed("keyword-fallback")
                .rawResponse("keyword match: " + category)
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String buildClassificationPrompt(EmailPayload payload) {
        return """
            Classify this email into EXACTLY ONE of these categories:
            INVOICE, SUPPORT, SPAM, ATTACHMENT, NEWSLETTER, URGENT, OTHER

            Email details:
            From: %s
            Subject: %s
            Body (first 300 chars): %s
            Has attachments: %s

            Rules:
            - INVOICE: billing, payment, receipt, order confirmation
            - SUPPORT: help request, bug report, ticket, issue
            - SPAM: promotional, marketing, unsubscribe
            - ATTACHMENT: has file attachments (and no other clear category)
            - NEWSLETTER: digest, weekly update, roundup
            - URGENT: needs immediate attention
            - OTHER: none of the above

            Respond with ONLY the category name, nothing else.
            Category:""".formatted(
                payload.getFrom(),
                payload.getSubject(),
                payload.getBodyPlain() != null
                    ? payload.getBodyPlain().substring(0, Math.min(300, payload.getBodyPlain().length()))
                    : "",
                payload.getAttachments() != null && !payload.getAttachments().isEmpty() ? "yes" : "no"
        );
    }

    private String buildEmailSummary(EmailPayload payload) {
        return "From: " + nullSafe(payload.getFrom())
             + " Subject: " + nullSafe(payload.getSubject())
             + " " + (payload.getBodyPlain() != null
                    ? payload.getBodyPlain().substring(0, Math.min(200, payload.getBodyPlain().length()))
                    : "");
    }

    private String extractCategory(String rawResponse) {
        String[] validCategories = {"INVOICE", "SUPPORT", "SPAM", "ATTACHMENT", "NEWSLETTER", "URGENT", "OTHER"};
        for (String cat : validCategories) {
            if (rawResponse.contains(cat)) return cat;
        }
        return "OTHER";
    }

    private String mapHuggingFaceLabel(String label) {
        return switch (label.toLowerCase()) {
            case "invoice"          -> "INVOICE";
            case "customer support" -> "SUPPORT";
            case "spam"             -> "SPAM";
            case "attachment"       -> "ATTACHMENT";
            case "newsletter"       -> "NEWSLETTER";
            case "urgent"           -> "URGENT";
            default                 -> "OTHER";
        };
    }

    private boolean matchesAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
