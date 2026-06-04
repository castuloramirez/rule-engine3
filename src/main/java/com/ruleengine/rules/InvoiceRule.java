package com.ruleengine.rules;

import com.ruleengine.model.ActionTracker;
import com.ruleengine.model.EmailPayload;
import org.springframework.stereotype.Component;

/**
 * InvoiceRule — fires when:
 *  - AI classified the email as INVOICE, OR
 *  - Subject contains "invoice", "billing", "payment", "receipt"
 *
 * Actions: SAVE_DB + NOTIFY_SLACK (simulated in logs)
 */
@Component
public class InvoiceRule implements EmailRule {

    @Override
    public String name() { return "InvoiceRule"; }

    @Override
    public int priority() { return 1; }

    @Override
    public boolean evaluate(EmailPayload payload) {
        // Primary: trust AI classification
        if ("INVOICE".equalsIgnoreCase(payload.getAiCategory())) {
            return true;
        }
        // Fallback: keyword check in subject
        String subject = nullSafe(payload.getSubject()).toLowerCase();
        return subject.contains("invoice")
            || subject.contains("billing")
            || subject.contains("payment")
            || subject.contains("receipt");
    }

    @Override
    public void execute(EmailPayload payload, ActionTracker tracker) {
        tracker.record(name(), "SAVE_DB");
        tracker.record(name(), "NOTIFY_SLACK");

        // Simulated action — replace with real implementations
        log.info("[InvoiceRule] 💰 SAVE_DB: Saving invoice email from {} | Subject: {}",
                payload.getFrom(), payload.getSubject());
        log.info("[InvoiceRule] 🔔 NOTIFY_SLACK: #invoices channel — New invoice from: {}",
                payload.getFrom());
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}
