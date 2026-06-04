package com.ruleengine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Payload sent by Zapier when a new Gmail email arrives.
 * Maps directly to the JSON body POSTed to /api/rules/evaluate
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailPayload {

    /** Sender email address */
    private String from;

    /** Recipient email address */
    private String to;

    /** Email subject line */
    private String subject;

    /** Plain text body */
    @JsonProperty("body_plain")
    private String bodyPlain;

    /** HTML body */
    @JsonProperty("body_html")
    private String bodyHtml;

    /** Email date string */
    private String date;

    /** Unique Gmail message ID */
    @JsonProperty("message_id")
    private String messageId;

    /** List of attachment URLs (Zapier uploads them to S3) */
    private List<String> attachments;

    /** AI-assigned category — populated by AIClassifierService, not Zapier */
    private String aiCategory;

    /** AI confidence score 0.0–1.0 */
    private Double aiConfidence;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** No-args constructor (required by Jackson) */
    public EmailPayload() {}

    /** All-args constructor (replaces @AllArgsConstructor) */
    public EmailPayload(String from, String to, String subject,
                        String bodyPlain, String bodyHtml, String date,
                        String messageId, List<String> attachments,
                        String aiCategory, Double aiConfidence) {
        this.from         = from;
        this.to           = to;
        this.subject      = subject;
        this.bodyPlain    = bodyPlain;
        this.bodyHtml     = bodyHtml;
        this.date         = date;
        this.messageId    = messageId;
        this.attachments  = attachments;
        this.aiCategory   = aiCategory;
        this.aiConfidence = aiConfidence;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getFrom()          { return from; }
    public String getTo()            { return to; }
    public String getSubject()       { return subject; }
    public String getBodyPlain()     { return bodyPlain; }
    public String getBodyHtml()      { return bodyHtml; }
    public String getDate()          { return date; }
    public String getMessageId()     { return messageId; }
    public List<String> getAttachments() { return attachments; }
    public String getAiCategory()    { return aiCategory; }
    public Double getAiConfidence()  { return aiConfidence; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setFrom(String from)             { this.from = from; }
    public void setTo(String to)                 { this.to = to; }
    public void setSubject(String subject)       { this.subject = subject; }
    public void setBodyPlain(String bodyPlain)   { this.bodyPlain = bodyPlain; }
    public void setBodyHtml(String bodyHtml)     { this.bodyHtml = bodyHtml; }
    public void setDate(String date)             { this.date = date; }
    public void setMessageId(String messageId)   { this.messageId = messageId; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
    public void setAiCategory(String aiCategory)         { this.aiCategory = aiCategory; }
    public void setAiConfidence(Double aiConfidence)     { this.aiConfidence = aiConfidence; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String from;
        private String to;
        private String subject;
        private String bodyPlain;
        private String bodyHtml;
        private String date;
        private String messageId;
        private List<String> attachments;
        private String aiCategory;
        private Double aiConfidence;

        public Builder from(String from)                     { this.from = from; return this; }
        public Builder to(String to)                         { this.to = to; return this; }
        public Builder subject(String subject)               { this.subject = subject; return this; }
        public Builder bodyPlain(String bodyPlain)           { this.bodyPlain = bodyPlain; return this; }
        public Builder bodyHtml(String bodyHtml)             { this.bodyHtml = bodyHtml; return this; }
        public Builder date(String date)                     { this.date = date; return this; }
        public Builder messageId(String messageId)           { this.messageId = messageId; return this; }
        public Builder attachments(List<String> attachments) { this.attachments = attachments; return this; }
        public Builder aiCategory(String aiCategory)         { this.aiCategory = aiCategory; return this; }
        public Builder aiConfidence(Double aiConfidence)     { this.aiConfidence = aiConfidence; return this; }

        public EmailPayload build() {
            return new EmailPayload(from, to, subject, bodyPlain, bodyHtml,
                    date, messageId, attachments,
                    aiCategory, aiConfidence);
        }
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "EmailPayload{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", subject='" + subject + '\'' +
                ", messageId='" + messageId + '\'' +
                ", aiCategory='" + aiCategory + '\'' +
                ", aiConfidence=" + aiConfidence +
                '}';
    }
}