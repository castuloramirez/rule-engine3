package com.ruleengine.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Passed through the rule engine facts so each rule can record
 * what it matched and what actions it triggered.
 */
@Getter
public class ActionTracker {

    private final List<String> matchedRules    = new ArrayList<>();
    private final List<String> actionsExecuted = new ArrayList<>();

    public void record(String ruleName, String action) {
        if (!matchedRules.contains(ruleName)) {
            matchedRules.add(ruleName);
        }
        actionsExecuted.add(action);
    }

    public boolean hasMatches() {
        return !matchedRules.isEmpty();
    }
}
