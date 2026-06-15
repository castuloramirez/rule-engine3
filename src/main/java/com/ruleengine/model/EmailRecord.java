package com.ruleengine.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * JPA entity — stores processed emails in H2 (in-memory) for testing.
 * View records at: http://localhost:8080/h2-console
 */
@Entity
@Table(name = "processed_emails")
public class EmailRecord {
    public EmailRecord() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true)
    private String messageId;

    private String fromAddress;
    private String toAddress;
    private String subject;

    @Column(length = 5000)
    private String bodyPreview;  // first 500 chars of body_plain

    private String aiCategory;
    private Double aiConfidence;
    private String aiModel;

    @Column(length = 500)
    private String matchedRules;   // comma-separated

    @Column(length = 500)
    private String actionsExecuted; // comma-separated

    private String status;

    @Column(name = "processed_at")
    private Instant processedAt;

    public EmailRecord(EmailPayload payload, RuleEngineResponse response) {
        this.messageId      = payload.getMessageId();
        this.fromAddress    = payload.getFrom();
        this.toAddress      = payload.getTo();
        this.subject        = payload.getSubject();
        this.bodyPreview    = payload.getBodyPlain() != null
                ? payload.getBodyPlain().substring(0, Math.min(500, payload.getBodyPlain().length()))
                : "";
        this.aiCategory     = response.getAiCategory();
        this.aiConfidence   = response.getAiConfidence();
        this.aiModel        = response.getAiModel();
        this.matchedRules   = response.getMatchedRules() != null
                ? String.join(", ", response.getMatchedRules()) : "";
        this.actionsExecuted = response.getActionsExecuted() != null
                ? String.join(", ", response.getActionsExecuted()) : "";
        this.status         = response.getStatus();
        this.processedAt    = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBodyPreview() {
        return bodyPreview;
    }

    public void setBodyPreview(String bodyPreview) {
        this.bodyPreview = bodyPreview;
    }

    public String getAiCategory() {
        return aiCategory;
    }

    public void setAiCategory(String aiCategory) {
        this.aiCategory = aiCategory;
    }

    public Double getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(Double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public String getMatchedRules() {
        return matchedRules;
    }

    public void setMatchedRules(String matchedRules) {
        this.matchedRules = matchedRules;
    }

    public String getActionsExecuted() {
        return actionsExecuted;
    }

    public void setActionsExecuted(String actionsExecuted) {
        this.actionsExecuted = actionsExecuted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}