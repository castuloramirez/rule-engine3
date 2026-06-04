package com.ruleengine.legacy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WorkflowStep — represents one row from your Dataverse new_mvpwkses table.
 *
 * In production this is loaded dynamically from Dataverse via DataverseController.
 * In this standalone version, steps are defined in YAML (rules-legacy.yml) or
 * programmatically, perfectly mirroring the Dataverse schema.
 *
 * Field mapping:
 *   new_wksid              → id
 *   new_wksname            → name
 *   new_type               → type         (SearchWord, Regex, Set, Get, If, etc.)
 *   new_searchmethod       → searchMethod (SearchWord, Regex, RegexFirstMatch)
 *   new_searchcondition    → condition    (Contains, Equals)
 *   new_searchterm         → searchTerm   (the value to look for)
 *   new_searchin           → searchIn     (Body, Subject, From, To, CC, BCC)
 *   new_searchcasesensitive→ caseSensitive("1"=yes, "0"=no)
 *   new_negation           → negation     ("1"=NOT, "0"=normal)
 *   new_updatecolumn       → updateColumn (column to SET)
 *   new_updatevalue        → updateValue  (value to SET)
 *   new_incl_excl          → inclExcl     (semicolon-separated inclusion/exclusion words)
 *   uniqa_new_inmemory     → inMemory     (memory key for IF-type checks)
 *   new_processid          → processId
 *   new_previous           → previous     (for branching)
 *   new_next               → next         (for branching)
 *   new_priority           → priority
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStep {

    private String id;
    private String name;

    /** Rule type: SearchWord | Regex | RegexFirstMatch | Set | Get | If | Declaration | SetVariable | Operation | Case | Placeholder | ColumnExpression | SendEmail | Attach | InclusionWords | ExclusionWords */
    private String type;

    /** SearchWord | Regex | RegexFirstMatch */
    private String searchMethod;

    /** Contains | Equals | gmi */
    private String condition;

    /** The search term(s) — semicolons separate multiple terms */
    private String searchTerm;

    /** Body, Subject, From, To, CC, BCC — comma separated for multiple fields */
    private String searchIn;

    /** "1" = case sensitive, "0" = case insensitive */
    @Builder.Default
    private String caseSensitive = "0";

    /** "1" = NOT (negation), "0" = normal */
    @Builder.Default
    private String negation = "0";

    /** Column to update when type=Set */
    private String updateColumn;

    /** Value to set when type=Set */
    private String updateValue;

    /** Semicolon-separated inclusion/exclusion words */
    private String inclExcl;

    /** Memory key for IF-type checks */
    private String inMemory;

    /** Parent process id */
    private String processId;

    /** Previous step id (for branching) */
    @Builder.Default
    private String previous = "null";

    /** Next step id (for branching, semicolons for multiple paths) */
    @Builder.Default
    private String next = "null";

    /** Error state id */
    private String errorStateValue;

    /** Success state id */
    private String successStateValue;

    /** Execution order */
    @Builder.Default
    private int priority = 10;
}
