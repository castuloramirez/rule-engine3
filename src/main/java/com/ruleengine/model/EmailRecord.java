package com.ruleengine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * JPA entity — stores processed emails in H2 (in-memory) for testing.
 * View records at: http://localhost:8080/h2-console
 */
@Entity
@Table(name = "processed_emails")
@Data
@NoArgsConstructor
public class EmailRecord {

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
}
