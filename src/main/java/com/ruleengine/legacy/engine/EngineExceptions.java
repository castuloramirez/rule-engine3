package com.ruleengine.legacy.engine;

/**
 * DecisionException — thrown when a rule step returns FALSE in a linear process.
 * Stops evaluation of the current process.
 * Mirrors: com.ruleengine.core.util.engine.DecisionException
 */
class DecisionException extends Exception {
    public DecisionException(String message) { super(message); }
}

/**
 * StopException — thrown by InclusionWords / ExclusionWords / Attachment validators.
 * Hard stop — skips remaining steps in current process.
 * Mirrors: com.ruleengine.core.util.engine.exception.StopException
 */
class StopException extends Exception {
    public StopException(String message) { super(message); }
}

/**
 * ContinueException — thrown by InclusionWords when a match is found.
 * Skips to next step (continue) rather than stopping.
 * Mirrors: com.ruleengine.core.util.engine.exception.ContinueException
 */
class ContinueException extends Exception {
    public ContinueException(String message) { super(message); }
}
