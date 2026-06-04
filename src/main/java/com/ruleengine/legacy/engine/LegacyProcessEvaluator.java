package com.ruleengine.legacy.engine;

import com.ruleengine.legacy.constants.EngineConstants;
import com.ruleengine.legacy.model.LegacyEngineResult;
import com.ruleengine.legacy.model.LegacyRuleProcess;
import com.ruleengine.legacy.model.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.StringTokenizer;

/**
 * LegacyProcessEvaluator — evaluates one LegacyRuleProcess against an email.
 *
 * Mirrors the core flow from RuleEngine.java:
 *   - getProcessWorkFlowsByProcess()     → linear step evaluation
 *   - evaluateTheProccessByThePath()     → path-based step evaluation
 *   - getListWorkFlowsDataByWorkFlowId() → per-step dispatch
 *   - scopeSearch()                      → actual matching
 *
 * For each step:
 *   - If step returns TRUE  → continue to next step
 *   - If step returns FALSE → throw DecisionException → stop this process
 *   - InclusionWords match  → throw ContinueException → skip to next step
 *   - ExclusionWords match  → throw StopException → stop this process
 */
@Slf4j
@RequiredArgsConstructor
public class LegacyProcessEvaluator {

    private final LegacyEngineContext context;
    private final MVPMemory           memory;

    /**
     * Run all steps in a process sequentially.
     * Returns true if all steps passed, false if stopped early.
     */
    public boolean evaluate(LegacyRuleProcess process,
                             String body, String subject, String from,
                             String to, String cc, String bcc,
                             LegacyEngineResult result) {

        if (!process.isEnabled()) {
            log.info("[Process:{}] Disabled — skipping", process.getProcessName());
            return false;
        }

        log.info("[Process:{}] Starting evaluation with {} steps",
                process.getProcessName(), process.getSteps().size());
        context.addLog("Process: " + process.getProcessName());
        result.getProcessesRun().add(process.getProcessName());

        LegacyScopeSearch scopeSearch = new LegacyScopeSearch(
                body, subject, from, to, cc, bcc, memory, context);

        boolean processPassed = true;

        for (WorkflowStep step : process.getSteps()) {

            // ---- InclusionWords / ExclusionWords ----
            if (EngineConstants.INCLUSION_WORDS.equals(step.getType())
             || EngineConstants.EXCLUSION_WORDS.equals(step.getType())) {
                try {
                    evaluateInclusionExclusion(step, body, subject);
                } catch (ContinueException e) {
                    log.debug("[Step:{}] ContinueException: {}", step.getId(), e.getMessage());
                    continue;
                } catch (StopException e) {
                    log.info("[Step:{}] StopException (Inclusion/Exclusion): {}", step.getId(), e.getMessage());
                    context.addLog("STOP: " + e.getMessage());
                    result.getStoppedAt().add(step.getId() + ":" + step.getName());
                    processPassed = false;
                    break;
                } catch (DecisionException e) {
                    log.info("[Step:{}] DecisionException: {}", step.getId(), e.getMessage());
                    result.getStoppedAt().add(step.getId() + ":" + step.getName());
                    processPassed = false;
                    break;
                }
                continue;
            }

            // ---- Standard steps (SearchWord / Regex / Set / Get / If / etc.) ----
            try {
                boolean passed = scopeSearch.evaluate(
                        step.getId(),
                        step.getType(),
                        step.getSearchMethod(),
                        step.getCondition(),
                        step.getSearchTerm(),
                        step.getSearchIn(),
                        step.getCaseSensitive(),
                        step.getUpdateColumn(),
                        step.getUpdateValue(),
                        step.getNegation(),
                        step.getInMemory(),
                        step.getName()
                );

                log.debug("[Step:{}:{}] result={}", step.getId(), step.getName(), passed);
                context.addLog("Step:" + step.getId() + ":" + step.getName() + "=" + passed);

                if (passed) {
                    result.getPassedSteps().add(step.getId() + ":" + step.getName());

                    // If a SET Category step fired, record it in memory
                    if (EngineConstants.SET.equals(step.getType())
                            && EngineConstants.CATEGORY.equalsIgnoreCase(step.getUpdateColumn())
                            && step.getUpdateValue() != null) {
                        memory.putValue(step.getUpdateValue(), step.getUpdateValue());
                    }
                } else {
                    // Step failed → DecisionException → stop this process
                    String error = MessageFormat.format(
                            EngineConstants.EXCEPTION_STOP_MSG,
                            process.getProcessId(), step.getId());
                    log.info("[Process:{}] Step [{}:{}] FAILED → stopping process",
                            process.getProcessName(), step.getId(), step.getName());
                    context.addLog("STOP:" + error);
                    result.getStoppedAt().add(step.getId() + ":" + step.getName());
                    processPassed = false;
                    break;
                }

            } catch (Exception e) {
                log.error("[Step:{}] exception: {}", step.getId(), e.getMessage(), e);
                context.addLog("ERROR:" + step.getId() + ":" + e.getMessage());
                result.getStoppedAt().add(step.getId() + ":" + step.getName());
                processPassed = false;
                break;
            }
        }

        // Sync context into result after each process
        syncContextToResult(result);
        return processPassed;
    }

    // -----------------------------------------------------------------------
    // InclusionWords / ExclusionWords logic
    // Mirrors typeInclusionExclusion() in RuleEngine.java
    // -----------------------------------------------------------------------
    private void evaluateInclusionExclusion(WorkflowStep step,
                                             String body, String subject)
            throws ContinueException, StopException, DecisionException {

        String inclExcl = step.getInclExcl();
        String searchIn = step.getSearchIn();

        StringTokenizer stIn = new StringTokenizer(searchIn, EngineConstants.DELIMITATOR_COMMA);
        boolean alreadyCheckedForExclusion = false;
        boolean continueForInclusion       = true;

        while (stIn.hasMoreTokens()) {
            String field = stIn.nextToken().toLowerCase();

            String textToSearch = EngineConstants.BODY.equalsIgnoreCase(field) ? body : subject;
            boolean wordFound = evaluateWords(textToSearch, inclExcl);

            if (EngineConstants.INCLUSION_WORDS.equals(step.getType())) {
                if (wordFound) {
                    throw new ContinueException("InclusionWord matched in " + field);
                }
                continueForInclusion = false;
            } else if (EngineConstants.EXCLUSION_WORDS.equals(step.getType())) {
                if (wordFound) {
                    throw new StopException(EngineConstants.EXCEPTION_STOP_MSG_E);
                }
                alreadyCheckedForExclusion = true;
            }
        }

        if (EngineConstants.INCLUSION_WORDS.equals(step.getType())) {
            if (!continueForInclusion) {
                throw new StopException(EngineConstants.EXCEPTION_STOP_MSG_I);
            }
        }

        if (EngineConstants.EXCLUSION_WORDS.equals(step.getType())) {
            if (alreadyCheckedForExclusion) {
                throw new ContinueException("");
            }
        }

        String error = MessageFormat.format(EngineConstants.EXCEPTION_STOP_MSG,
                step.getProcessId(), step.getId());
        throw new DecisionException(error);
    }

    /** Check if any of the semicolon-separated words exist in the text */
    private boolean evaluateWords(String text, String inclExcl) {
        if (inclExcl == null || text == null) return false;
        StringTokenizer st = new StringTokenizer(inclExcl, EngineConstants.DELIMITATOR_SEMICOLON);
        while (st.hasMoreTokens()) {
            String word = st.nextToken().trim();
            if (LegacyScopeSearch_Helper.checkContains(text, word)) return true;
        }
        return false;
    }

    private void syncContextToResult(LegacyEngineResult result) {
        result.setCategory(context.getCurrentEmailCategory());
        result.setStatus(context.getStatusMvp());
        result.setClient(context.getClient());
        result.setClaimNumber(context.getClaimNumber());
        result.setPolicyNumber(context.getPolicyNumber());
        result.setBranch(context.getBranch());
        result.setColumnUpdates(new java.util.ArrayList<>(context.getColumnUpdates()));
        result.setLog(new java.util.ArrayList<>(context.getLog()));
    }
}

/** Small helper to avoid circular dependency */
class LegacyScopeSearch_Helper {
    static boolean checkContains(String text, String term) {
        if (text == null || term == null) return false;
        return text.toLowerCase().contains(term.toLowerCase());
    }
}
