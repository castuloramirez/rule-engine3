package com.ruleengine.legacy.engine;

import com.ruleengine.legacy.model.LegacyRuleProcess;
import com.ruleengine.legacy.model.WorkflowStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * LegacyRulesConfig — loads rules-legacy.yml into Spring beans.
 *
 * Usage in application.properties:
 *   spring.config.import=classpath:rules-legacy.yml
 *
 * Or just having rules-legacy.yml in src/main/resources is enough since
 * Spring Boot auto-loads YAML from classpath.
 */
@Configuration
public class LegacyRulesConfig {

    private static final Logger log = LogManager.getLogger(LegacyRulesConfig.class);

    @Bean
    @ConfigurationProperties(prefix = "legacy")
    public LegacyProperties legacyProperties() {
        return new LegacyProperties();
    }

    @Bean
    public List<LegacyRuleProcess> legacyProcesses(LegacyProperties props) {
        List<LegacyRuleProcess> processes = new ArrayList<>();

        if (props.getProcesses() == null) {
            log.warn("No legacy processes configured in rules-legacy.yml");
            return processes;
        }

        for (LegacyProperties.ProcessDef def : props.getProcesses()) {
            LegacyRuleProcess process = LegacyRuleProcess.builder()
                    .processId(def.getProcessId())
                    .processName(def.getProcessName())
                    .enabled(def.isEnabled())
                    .branching(def.isBranching())
                    .steps(buildSteps(def.getSteps()))
                    .build();
            processes.add(process);
            log.info("Loaded process: {} ({} steps, enabled={})",
                    def.getProcessName(),
                    process.getSteps().size(),
                    def.isEnabled());
        }

        log.info("Total legacy processes loaded: {}", processes.size());
        return processes;
    }

    private List<WorkflowStep> buildSteps(List<LegacyProperties.StepDef> defs) {
        List<WorkflowStep> steps = new ArrayList<>();
        if (defs == null) return steps;

        for (LegacyProperties.StepDef d : defs) {
            WorkflowStep step = new WorkflowStep(
                    d.getId(),
                    d.getName(),
                    d.getType(),
                    d.getSearchMethod(),
                    d.getCondition(),
                    d.getSearchTerm(),
                    d.getSearchIn(),
                    d.getCaseSensitive(),   // compact constructor normalises null → "0"
                    d.getNegation(),        // compact constructor normalises null → "0"
                    d.getUpdateColumn(),
                    d.getUpdateValue(),
                    d.getInclExcl(),
                    d.getInMemory(),
                    d.getProcessId(),
                    d.getPrevious(),        // compact constructor normalises null → "null"
                    d.getNext(),            // compact constructor normalises null → "null"
                    d.getErrorStateValue(),
                    d.getSuccessStateValue(),
                    d.getPriority()
            );
            steps.add(step);
        }

        // Sort by priority
        steps.sort(java.util.Comparator.comparingInt(WorkflowStep::priority));
        return steps;
    }

    // -----------------------------------------------------------------------
    // POJO classes bound to rules-legacy.yml
    // -----------------------------------------------------------------------

    public static class LegacyProperties {

        private List<ProcessDef> processes;

        public List<ProcessDef> getProcesses() {
            return processes;
        }

        public void setProcesses(List<ProcessDef> processes) {
            this.processes = processes;
        }

        public static class ProcessDef {
            private String          processId;
            private String          processName;
            private boolean         enabled   = true;
            private boolean         branching = false;
            private List<StepDef>   steps;

            public String getProcessId() {
                return processId;
            }

            public void setProcessId(String processId) {
                this.processId = processId;
            }

            public String getProcessName() {
                return processName;
            }

            public void setProcessName(String processName) {
                this.processName = processName;
            }

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public boolean isBranching() {
                return branching;
            }

            public void setBranching(boolean branching) {
                this.branching = branching;
            }

            public List<StepDef> getSteps() {
                return steps;
            }

            public void setSteps(List<StepDef> steps) {
                this.steps = steps;
            }
        }

        public static class StepDef {
            private String id;
            private String name;
            private String type;
            private String searchMethod;
            private String condition;
            private String searchTerm;
            private String searchIn;
            private String caseSensitive = "0";
            private String negation      = "0";
            private String updateColumn;
            private String updateValue;
            private String inclExcl;
            private String inMemory;
            private String processId;
            private String previous = "null";
            private String next     = "null";
            private String errorStateValue;
            private String successStateValue;
            private int    priority  = 10;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getSearchMethod() {
                return searchMethod;
            }

            public void setSearchMethod(String searchMethod) {
                this.searchMethod = searchMethod;
            }

            public String getCondition() {
                return condition;
            }

            public void setCondition(String condition) {
                this.condition = condition;
            }

            public String getSearchTerm() {
                return searchTerm;
            }

            public void setSearchTerm(String searchTerm) {
                this.searchTerm = searchTerm;
            }

            public String getSearchIn() {
                return searchIn;
            }

            public void setSearchIn(String searchIn) {
                this.searchIn = searchIn;
            }

            public String getCaseSensitive() {
                return caseSensitive;
            }

            public void setCaseSensitive(String caseSensitive) {
                this.caseSensitive = caseSensitive;
            }

            public String getNegation() {
                return negation;
            }

            public void setNegation(String negation) {
                this.negation = negation;
            }

            public String getUpdateColumn() {
                return updateColumn;
            }

            public void setUpdateColumn(String updateColumn) {
                this.updateColumn = updateColumn;
            }

            public String getUpdateValue() {
                return updateValue;
            }

            public void setUpdateValue(String updateValue) {
                this.updateValue = updateValue;
            }

            public String getInclExcl() {
                return inclExcl;
            }

            public void setInclExcl(String inclExcl) {
                this.inclExcl = inclExcl;
            }

            public String getInMemory() {
                return inMemory;
            }

            public void setInMemory(String inMemory) {
                this.inMemory = inMemory;
            }

            public String getProcessId() {
                return processId;
            }

            public void setProcessId(String processId) {
                this.processId = processId;
            }

            public String getPrevious() {
                return previous;
            }

            public void setPrevious(String previous) {
                this.previous = previous;
            }

            public String getNext() {
                return next;
            }

            public void setNext(String next) {
                this.next = next;
            }

            public String getErrorStateValue() {
                return errorStateValue;
            }

            public void setErrorStateValue(String errorStateValue) {
                this.errorStateValue = errorStateValue;
            }

            public String getSuccessStateValue() {
                return successStateValue;
            }

            public void setSuccessStateValue(String successStateValue) {
                this.successStateValue = successStateValue;
            }

            public int getPriority() {
                return priority;
            }

            public void setPriority(int priority) {
                this.priority = priority;
            }
        }
    }
}