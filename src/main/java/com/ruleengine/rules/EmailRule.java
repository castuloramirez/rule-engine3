package com.ruleengine.rules;

import com.ruleengine.model.EmailPayload;
import com.ruleengine.model.ActionTracker;

/**
 * Base interface for all email rules.
 * Every rule must implement:
 *   - name()     — unique rule identifier
 *   - priority() — lower number = higher priority
 *   - evaluate() — return true if this rule should fire
 *   - execute()  — actions to run when rule fires
 */
public interface EmailRule {

    String name();

    int priority();

    boolean evaluate(EmailPayload payload);

    void execute(EmailPayload payload, ActionTracker tracker);
}
