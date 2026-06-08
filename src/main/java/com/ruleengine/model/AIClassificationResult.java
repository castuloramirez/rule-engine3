package com.ruleengine.model;

/**
 * Result returned by AIClassifierService.
 * Contains the AI-assigned category, confidence, and which model was used.
 */
public record AIClassificationResult(

        /** One of: INVOICE, SUPPORT, SPAM, ATTACHMENT, NEWSLETTER, URGENT, OTHER */
        String category,

        /** Confidence 0.0 to 1.0 (where available from the AI model) */
        Double confidence,

        /** Which AI model/provider was used: "ollama-llama3", "huggingface", "keyword-fallback" */
        String modelUsed,

        /** Raw response text from the AI for debugging */
        String rawResponse

) {
    public AIClassificationResult {
        if (confidence == null) {
            confidence = 1.0;
        }
    }
}