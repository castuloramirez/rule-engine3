package com.ruleengine.legacy.model;

/**
 * WorkflowStep — represents one row from your Dataverse new_mvpwkses table.
 *
 * Field mapping:
 *   new_wksid              → id
 *   new_wksname            → name
 *   new_type               → type
 *   new_searchmethod       → searchMethod
 *   new_searchcondition    → condition
 *   new_searchterm         → searchTerm
 *   new_searchin           → searchIn
 *   new_searchcasesensitive→ caseSensitive ("1"=yes, "0"=no)
 *   new_negation           → negation     ("1"=NOT, "0"=normal)
 *   new_updatecolumn       → updateColumn
 *   new_updatevalue        → updateValue
 *   new_incl_excl          → inclExcl
 *   uniqa_new_inmemory     → inMemory
 *   new_processid          → processId
 *   new_previous           → previous
 *   new_next               → next
 *   new_priority           → priority
 */
public record WorkflowStep(

        String id,
        String name,

        /** SearchWord | Regex | RegexFirstMatch | Set | Get | If |
         *  Declaration | SetVariable | Operation | Case |
         *  Placeholder | ColumnExpression | SendEmail |
         *  Attach | InclusionWords | ExclusionWords */
        String type,

        /** SearchWord | Regex | RegexFirstMatch */
        String searchMethod,

        /** Contains | Equals | gmi */
        String condition,

        /** Semicolons separate multiple terms */
        String searchTerm,

        /** Body, Subject, From, To, CC, BCC — comma-separated */
        String searchIn,

        /** "1" = case sensitive, "0" = case insensitive */
        String caseSensitive,

        /** "1" = NOT (negation), "0" = normal */
        String negation,

        /** Column to update when type=Set */
        String updateColumn,

        /** Value to set when type=Set */
        String updateValue,

        /** Semicolon-separated inclusion/exclusion words */
        String inclExcl,

        /** Memory key for IF-type checks */
        String inMemory,

        /** Parent process id */
        String processId,

        /** Previous step id (for branching) */
        String previous,

        /** Next step id (for branching, semicolons for multiple paths) */
        String next,

        /** Error state id */
        String errorStateValue,

        /** Success state id */
        String successStateValue,

        /** Execution order */
        int priority

) {
    // Compact constructor — normalises defaults before fields are sealed
    public WorkflowStep {
        caseSensitive    = defaultIfBlank(caseSensitive,    "0");
        negation         = defaultIfBlank(negation,         "0");
        previous         = defaultIfBlank(previous,         "null");
        next             = defaultIfBlank(next,             "null");
        errorStateValue  = defaultIfNull(errorStateValue,   null);
        successStateValue = defaultIfNull(successStateValue, null);
        // int fields cannot be null, so priority just needs a range guard
        if (priority <= 0) priority = 10;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String defaultIfNull(String value, String fallback) {
        return value != null ? value : fallback;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String searchMethod() {
        return searchMethod;
    }

    @Override
    public String condition() {
        return condition;
    }

    @Override
    public String searchTerm() {
        return searchTerm;
    }

    @Override
    public String searchIn() {
        return searchIn;
    }

    @Override
    public String caseSensitive() {
        return caseSensitive;
    }

    @Override
    public String negation() {
        return negation;
    }

    @Override
    public String updateColumn() {
        return updateColumn;
    }

    @Override
    public String updateValue() {
        return updateValue;
    }

    @Override
    public String inclExcl() {
        return inclExcl;
    }

    @Override
    public String inMemory() {
        return inMemory;
    }

    @Override
    public String processId() {
        return processId;
    }

    @Override
    public String previous() {
        return previous;
    }

    @Override
    public String next() {
        return next;
    }

    @Override
    public String errorStateValue() {
        return errorStateValue;
    }

    @Override
    public String successStateValue() {
        return successStateValue;
    }

    @Override
    public int priority() {
        return priority;
    }
}