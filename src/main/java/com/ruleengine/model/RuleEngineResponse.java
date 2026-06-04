package com.ruleengine.model;

import java.time.Instant;
import java.util.List;

/**
 * Response returned by the rule engine after processing an email.
 * Zapier receives this back as the webhook response body.
 */
public class RuleEngineResponse {

    /** The Gmail message ID from the input payload */
    private String messageId;

    /** Names of all rules that matched and fired */
    private List<String> matchedRules;

    /** Actions that were executed (e.g. SAVE_DB, NOTIFY_SLACK) */
    private List<String> actionsExecuted;

    /** "processed" | "no_match" | "error" */
    private String status;

    /** AI classification result */
    private String aiCategory;

    /** AI confidence score */
    private Double aiConfidence;

    /** AI model used */
    private String aiModel;

    /** Processing timestamp — defaults to now, like @Builder.Default */
    private String processedAt = Instant.now().toString();

    /** Error message if status = "error" */
    private String errorMessage;

    // ── Constructors ──────────────────────────────────────────────────────────

    public RuleEngineResponse() {}

    public RuleEngineResponse(String messageId, List<String> matchedRules,
                              List<String> actionsExecuted, String status,
                              String aiCategory, Double aiConfidence,
                              String aiModel, String processedAt,
                              String errorMessage) {
        this.messageId       = messageId;
        this.matchedRules    = matchedRules;
        this.actionsExecuted = actionsExecuted;
        this.status          = status;
        this.aiCategory      = aiCategory;
        this.aiConfidence    = aiConfidence;
        this.aiModel         = aiModel;
        this.processedAt     = (processedAt != null) ? processedAt : Instant.now().toString();
        this.errorMessage    = errorMessage;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getMessageId()            { return messageId; }
    public List<String> getMatchedRules()   { return matchedRules; }
    public List<String> getActionsExecuted(){ return actionsExecuted; }
    public String getStatus()               { return status; }
    public String getAiCategory()           { return aiCategory; }
    public Double getAiConfidence()         { return aiConfidence; }
    public String getAiModel()              { return aiModel; }
    public String getProcessedAt()          { return processedAt; }
    public String getErrorMessage()         { return errorMessage; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setMessageId(String messageId)                   { this.messageId = messageId; }
    public void setMatchedRules(List<String> matchedRules)       { this.matchedRules = matchedRules; }
    public void setActionsExecuted(List<String> actionsExecuted) { this.actionsExecuted = actionsExecuted; }
    public void setStatus(String status)                         { this.status = status; }
    public void setAiCategory(String aiCategory)                 { this.aiCategory = aiCategory; }
    public void setAiConfidence(Double aiConfidence)             { this.aiConfidence = aiConfidence; }
    public void setAiModel(String aiModel)                       { this.aiModel = aiModel; }
    public void setProcessedAt(String processedAt)               { this.processedAt = processedAt; }
    public void setErrorMessage(String errorMessage)             { this.errorMessage = errorMessage; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String messageId;
        private List<String> matchedRules;
        private List<String> actionsExecuted;
        private String status;
        private String aiCategory;
        private Double aiConfidence;
        private String aiModel;
        private String processedAt;
        private String errorMessage;

        public Builder messageId(String messageId)                   { this.messageId = messageId; return this; }
        public Builder matchedRules(List<String> matchedRules)       { this.matchedRules = matchedRules; return this; }
        public Builder actionsExecuted(List<String> actionsExecuted) { this.actionsExecuted = actionsExecuted; return this; }
        public Builder status(String status)                         { this.status = status; return this; }
        public Builder aiCategory(String aiCategory)                 { this.aiCategory = aiCategory; return this; }
        public Builder aiConfidence(Double aiConfidence)             { this.aiConfidence = aiConfidence; return this; }
        public Builder aiModel(String aiModel)                       { this.aiModel = aiModel; return this; }
        public Builder processedAt(String processedAt)               { this.processedAt = processedAt; return this; }
        public Builder errorMessage(String errorMessage)             { this.errorMessage = errorMessage; return this; }

        public RuleEngineResponse build() {
            return new RuleEngineResponse(messageId, matchedRules, actionsExecuted,
                    status, aiCategory, aiConfidence,
                    aiModel, processedAt, errorMessage);
        }
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "RuleEngineResponse{" +
                "messageId='" + messageId + '\'' +
                ", status='" + status + '\'' +
                ", aiCategory='" + aiCategory + '\'' +
                ", aiConfidence=" + aiConfidence +
                ", matchedRules=" + matchedRules +
                ", actionsExecuted=" + actionsExecuted +
                ", processedAt='" + processedAt + '\'' +
                '}';
    }
}