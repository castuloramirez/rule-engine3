package com.ruleengine.legacy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * LegacyRuleProcess — mirrors a row in new_mvpprocesses + its workflow steps.
 *
 * In production: loaded from Dataverse.
 * In standalone: loaded from rules-legacy.yml.
 *
 * Each process contains an ordered list of WorkflowStep entries.
 * Steps are evaluated in sequence — if one fails (returns false), the
 * process stops (DecisionException), just like the original engine.
 */
public class LegacyRuleProcess {

    /** new_processid */
    private String processId;

    /** new_processname — human-readable name */
    private String processName;

    /** Whether this process is enabled (new_isenabled = "1") */
    private boolean enabled = true;

    /** Ordered list of workflow steps for this process */
    private List<WorkflowStep> steps = new ArrayList<>();

    /**
     * Whether to use branching path evaluation (getBranching() == "1").
     * false = linear evaluation (getProccess)
     * true  = branching path evaluation (getProccessBranchingNewPath)
     */
    private boolean branching = false;

    // ── Constructors ──────────────────────────────────────────────────────────

    public LegacyRuleProcess() {}

    public LegacyRuleProcess(String processId, String processName,
                             boolean enabled, List<WorkflowStep> steps,
                             boolean branching) {
        this.processId   = processId;
        this.processName = processName;
        this.enabled     = enabled;
        this.steps       = (steps != null) ? steps : new ArrayList<>();
        this.branching   = branching;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getProcessId()           { return processId; }
    public String getProcessName()         { return processName; }
    public boolean isEnabled()             { return enabled; }
    public List<WorkflowStep> getSteps()   { return steps; }
    public boolean isBranching()           { return branching; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setProcessId(String processId)     { this.processId = processId; }
    public void setProcessName(String processName) { this.processName = processName; }
    public void setEnabled(boolean enabled)        { this.enabled = enabled; }
    public void setBranching(boolean branching)    { this.branching = branching; }
    public void setSteps(List<WorkflowStep> steps) {
        this.steps = (steps != null) ? steps : new ArrayList<>();
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String processId;
        private String processName;
        private boolean enabled   = true;           // mirrors @Builder.Default
        private List<WorkflowStep> steps = new ArrayList<>(); // mirrors @Builder.Default
        private boolean branching = false;          // mirrors @Builder.Default

        public Builder processId(String processId)         { this.processId = processId; return this; }
        public Builder processName(String processName)     { this.processName = processName; return this; }
        public Builder enabled(boolean enabled)            { this.enabled = enabled; return this; }
        public Builder steps(List<WorkflowStep> steps)     { this.steps = steps; return this; }
        public Builder branching(boolean branching)        { this.branching = branching; return this; }

        public LegacyRuleProcess build() {
            return new LegacyRuleProcess(processId, processName, enabled, steps, branching);
        }
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "LegacyRuleProcess{" +
                "processId='" + processId + '\'' +
                ", processName='" + processName + '\'' +
                ", enabled=" + enabled +
                ", branching=" + branching +
                ", steps=" + steps +
                '}';
    }
}