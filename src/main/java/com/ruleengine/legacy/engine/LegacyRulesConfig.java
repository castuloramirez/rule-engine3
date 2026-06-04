package com.ruleengine.legacy.engine;

import com.ruleengine.legacy.model.LegacyRuleProcess;
import com.ruleengine.legacy.model.WorkflowStep;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Configuration
public class LegacyRulesConfig {

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
            WorkflowStep step = WorkflowStep.builder()
                    .id(           d.getId())
                    .name(         d.getName())
                    .type(         d.getType())
                    .searchMethod( d.getSearchMethod())
                    .condition(    d.getCondition())
                    .searchTerm(   d.getSearchTerm())
                    .searchIn(     d.getSearchIn())
                    .caseSensitive(d.getCaseSensitive() != null ? d.getCaseSensitive() : "0")
                    .negation(     d.getNegation()      != null ? d.getNegation()      : "0")
                    .updateColumn( d.getUpdateColumn())
                    .updateValue(  d.getUpdateValue())
                    .inclExcl(     d.getInclExcl())
                    .inMemory(     d.getInMemory())
                    .priority(     d.getPriority())
                    .previous(     d.getPrevious() != null ? d.getPrevious() : "null")
                    .next(         d.getNext()     != null ? d.getNext()     : "null")
                    .build();
            steps.add(step);
        }

        // Sort by priority
        steps.sort(java.util.Comparator.comparingInt(WorkflowStep::getPriority));
        return steps;
    }

    // -----------------------------------------------------------------------
    // POJO classes bound to rules-legacy.yml
    // -----------------------------------------------------------------------

    @Data
    public static class LegacyProperties {

        private List<ProcessDef> processes;

        @Data
        public static class ProcessDef {
            private String          processId;
            private String          processName;
            private boolean         enabled   = true;
            private boolean         branching = false;
            private List<StepDef>   steps;
        }

        @Data
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
        }
    }
}
