package com.ruleengine.rules;
import com.ruleengine.model.ActionTracker;
import com.ruleengine.model.EmailPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

// ============================================================================
// AttachmentRule
// ============================================================================


@Component
class AttachmentRule implements EmailRule {

    private static final Logger log = LogManager.getLogger(AttachmentRule.class);

    @Override public String name()     { return "AttachmentRule"; }
    @Override public int    priority() { return 3; }

    @Override
    public boolean evaluate(EmailPayload payload) {
        if ("ATTACHMENT".equalsIgnoreCase(payload.getAiCategory())) return true;
        return payload.getAttachments() != null && !payload.getAttachments().isEmpty();
    }

    @Override
    public void execute(EmailPayload payload, ActionTracker tracker) {
        tracker.record(name(), "UPLOAD_S3");
        tracker.record(name(), "LOG");
        log.info("[AttachmentRule] 📎 UPLOAD_S3: {} attachment(s) from {} | {}",
                payload.getAttachments().size(), payload.getFrom(), payload.getSubject());
    }
}

// ============================================================================
// SpamRule
// ============================================================================

@Component
class SpamRule implements EmailRule {
    private static final Logger log = LogManager.getLogger(SpamRule.class);

    @Override public String name()     { return "SpamRule"; }
    @Override public int    priority() { return 4; }

    @Override
    public boolean evaluate(EmailPayload payload) {
        if ("SPAM".equalsIgnoreCase(payload.getAiCategory())) return true;
        String body    = nullSafe(payload.getBodyPlain()).toLowerCase();
        String subject = nullSafe(payload.getSubject()).toLowerCase();
        return body.contains("unsubscribe")
            || subject.contains("unsubscribe")
            || body.contains("opt-out")
            || subject.contains("promo")
            || subject.contains("discount")
            || subject.contains("deal");
    }

    @Override
    public void execute(EmailPayload payload, ActionTracker tracker) {
        tracker.record(name(), "MARK_SPAM");
        tracker.record(name(), "SKIP");
        log.info("[SpamRule] 🗑️  MARK_SPAM: {} | {}", payload.getFrom(), payload.getSubject());
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}

// ============================================================================
// UrgentRule
// ============================================================================

@Component
class UrgentRule implements EmailRule {

    private static final Logger log = LogManager.getLogger(UrgentRule.class);
    @Override public String name()     { return "UrgentRule"; }
    @Override public int    priority() { return 5; }

    @Override
    public boolean evaluate(EmailPayload payload) {
        if ("URGENT".equalsIgnoreCase(payload.getAiCategory())) return true;
        String subject = nullSafe(payload.getSubject()).toLowerCase();
        return subject.contains("urgent")
            || subject.contains("asap")
            || subject.contains("critical")
            || subject.contains("emergency")
            || subject.contains("immediately");
    }

    @Override
    public void execute(EmailPayload payload, ActionTracker tracker) {
        tracker.record(name(), "SEND_SMS_ALERT");
        tracker.record(name(), "NOTIFY_MANAGER");
        log.info("[UrgentRule] 🚨 SEND_SMS_ALERT: Urgent email from {} | {}", payload.getFrom(), payload.getSubject());
        log.info("[UrgentRule] 📣 NOTIFY_MANAGER: Escalating to manager");
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}

// ============================================================================
// NewsletterRule
// ============================================================================

@Component
class NewsletterRule implements EmailRule {
    private static final Logger log = LogManager.getLogger(NewsletterRule.class);
    @Override public String name()     { return "NewsletterRule"; }
    @Override public int    priority() { return 6; }

    @Override
    public boolean evaluate(EmailPayload payload) {
        if ("NEWSLETTER".equalsIgnoreCase(payload.getAiCategory())) return true;
        String subject = nullSafe(payload.getSubject()).toLowerCase();
        String body    = nullSafe(payload.getBodyPlain()).toLowerCase();
        return subject.contains("newsletter")
            || subject.contains("weekly")
            || subject.contains("digest")
            || subject.contains("roundup")
            || body.contains("you're receiving this because you subscribed");
    }

    @Override
    public void execute(EmailPayload payload, ActionTracker tracker) {
        tracker.record(name(), "ARCHIVE");
        tracker.record(name(), "LABEL_NEWSLETTER");
        log.info("[NewsletterRule] 📰 ARCHIVE + LABEL: Newsletter from {}", payload.getFrom());
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}

// ============================================================================
// FallbackRule — always fires if nothing else matched
// ============================================================================


@Component
class FallbackRule implements EmailRule {
    private static final Logger log = LogManager.getLogger(FallbackRule.class);
    @Override public String name()     { return "FallbackRule"; }
    @Override public int    priority() { return 99; }

    @Override
    public boolean evaluate(EmailPayload payload) {
        // Always true — this is the catch-all
        return true;
    }

    @Override
    public void execute(EmailPayload payload, ActionTracker tracker) {
        tracker.record(name(), "LOG_ONLY");
        log.info("[FallbackRule] 📝 LOG_ONLY: Unclassified email from {} | {}", payload.getFrom(), payload.getSubject());
    }
}
