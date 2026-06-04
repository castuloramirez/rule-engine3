package com.ruleengine.rules;

import com.ruleengine.model.ActionTracker;
import com.ruleengine.model.EmailPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * SupportRule — fires when:
 *  - AI classified the email as SUPPORT, OR
 *  - From address ends with a support domain
 *  - Subject contains "support", "help", "ticket", "issue", "bug"
 *
 * Actions: CREATE_TICKET + SEND_ACK
 */

@Component
public class SupportRule implements EmailRule {

    private static final Logger log = LogManager.getLogger(SupportRule.class);

    @Override
    public String name() { return "SupportRule"; }

    @Override
    public int priority() { return 2; }

    @Override
    public boolean evaluate(EmailPayload payload) {
        if ("SUPPORT".equalsIgnoreCase(payload.getAiCategory())) {
            return true;
        }
        String from    = nullSafe(payload.getFrom()).toLowerCase();
        String subject = nullSafe(payload.getSubject()).toLowerCase();
        return from.endsWith("@support.com")
            || from.contains("support")
            || subject.contains("support")
            || subject.contains("help")
            || subject.contains("ticket")
            || subject.contains("issue")
            || subject.contains("bug");
    }

    @Override
    public void execute(EmailPayload payload, ActionTracker tracker) {
        tracker.record(name(), "CREATE_TICKET");
        tracker.record(name(), "SEND_ACK");

        log.info("[SupportRule] 🎫 CREATE_TICKET: New support request from {} | {}",
                payload.getFrom(), payload.getSubject());
        log.info("[SupportRule] ✉️  SEND_ACK: Sending acknowledgement to {}", payload.getFrom());
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}
