package com.ruleengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result returned by AIClassifierService.
 * Contains the AI-assigned category, confidence, and which model was used.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIClassificationResult {

    /**
     * One of: INVOICE, SUPPORT, SPAM, ATTACHMENT, NEWSLETTER, URGENT, OTHER
     */
    private String category;

    /**
     * Confidence 0.0 to 1.0 (where available from the AI model)
     */
    @Builder.Default
    private Double confidence = 1.0;

    /**
     * Which AI model/provider was used: "ollama-llama3", "huggingface", "keyword-fallback"
     */
    private String modelUsed;

    /**
     * Raw response text from the AI for debugging
     */
    private String rawResponse;
}
