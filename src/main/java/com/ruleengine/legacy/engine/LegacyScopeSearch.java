package com.ruleengine.legacy.engine;

import com.ruleengine.legacy.constants.EngineConstants;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * LegacyScopeSearch — exact port of scopeSearch() from your production RuleEngine.java.
 *
 * This is the core matching logic that evaluates one WorkflowStep against the email fields.
 * Supports all search methods:
 *   - SearchWord (Contains / Equals) with case sensitivity + negation
 *   - Regex (count of matches)
 *   - RegexFirstMatch (extract first match into a column)
 *
 * All logic mirrors the original exactly — same StringTokenizer delimiters,
 * same negation logic, same field routing (Body/Subject/From/To/CC/BCC).
 */
@Slf4j
public class LegacyScopeSearch {

    private final String body;
    private final String subject;
    private final String from;
    private final String to;
    private final String cc;
    private final String bcc;
    private final MVPMemory memory;
    private final LegacyEngineContext context;

    public LegacyScopeSearch(String body, String subject, String from,
                              String to, String cc, String bcc,
                              MVPMemory memory, LegacyEngineContext context) {
        this.body    = nullSafe(body);
        this.subject = nullSafe(subject);
        this.from    = nullSafe(from);
        this.to      = nullSafe(to);
        this.cc      = nullSafe(cc);
        this.bcc     = nullSafe(bcc);
        this.memory  = memory;
        this.context = context;
    }

    /**
     * Evaluates one step. Returns true = step passed, false = step failed (DecisionException follows).
     *
     * @param wksid              step id
     * @param type               rule type (SearchWord, Set, Get, If, etc.)
     * @param searchMethod       SearchWord | Regex | RegexFirstMatch
     * @param searchCondition    Contains | Equals | gmi
     * @param searchTerm         term(s) to search — semicolons separate multiple
     * @param searchIn           Body, Subject, From, To, CC, BCC — comma separated
     * @param caseSensitive      "1" = sensitive, "0" = insensitive
     * @param updateColumn       column to SET (for Set/RegexFirstMatch)
     * @param updateValue        value to SET (for Set type)
     * @param negation           "1" = NOT logic, "0" = normal
     * @param inMemory           memory key for IF type
     * @param wksName            step name (for Declaration/SetVariable/Operation/Case)
     */
    public boolean evaluate(String wksid, String type, String searchMethod, String searchCondition,
                             String searchTerm, String searchIn, String caseSensitive,
                             String updateColumn, String updateValue, String negation,
                             String inMemory, String wksName) throws Exception {

        // ----------------------------------------------------------------
        // SEARCHWORD — Contains / Equals
        // ----------------------------------------------------------------
        if (EngineConstants.SEARCHWORD.equals(searchMethod)) {

            if (isEmpty(searchIn) || isEmpty(searchTerm)) return false;
            if (isEmpty(negation)) negation = "";

            String termForContains = searchTerm; // preserve original case for Contains
            searchIn = searchIn.trim();

            StringTokenizer stIn = new StringTokenizer(searchIn, EngineConstants.DELIMITATOR_COMMA);

            // ---- CONTAINS ----
            if (EngineConstants.CONTAINS.equals(searchCondition)) {
                log.debug("[SearchWord/Contains] term={} searchIn={}", termForContains, searchIn);

                while (stIn.hasMoreTokens()) {
                    String field = stIn.nextToken();
                    StringTokenizer stTerm = new StringTokenizer(termForContains, EngineConstants.DELIMITATOR_SEMICOLON);

                    while (stTerm.hasMoreTokens()) {
                        String term = stTerm.nextToken();
                        String fieldValue = getFieldValue(field);

                        if (StringHelper.checkContainsCaseSensitive(fieldValue, term, caseSensitive)) {
                            if (EngineConstants._YES.equals(negation)) return false; // NOT CONTAINS
                            return true;
                        }
                    }
                }

                // Not found in any field
                if (EngineConstants._YES.equals(negation)) return true;
                return false;
            }

            // ---- EQUALS ----
            if (EngineConstants.EQUALS.equals(searchCondition)) {
                log.debug("[SearchWord/Equals] term={} searchIn={}", searchTerm, searchIn);
                boolean isFound = false;

                while (stIn.hasMoreTokens()) {
                    String field = stIn.nextToken().trim();
                    StringTokenizer stTerm = new StringTokenizer(searchTerm, EngineConstants.DELIMITATOR_SEMICOLON);
                    String fieldValue = getFieldValue(field);

                    while (stTerm.hasMoreTokens()) {
                        String term = stTerm.nextToken().trim();

                        boolean matched;
                        if (EngineConstants._YES.equals(caseSensitive)) {
                            matched = term.equals(fieldValue);
                        } else {
                            matched = term.toLowerCase().equals(fieldValue.toLowerCase());
                        }

                        if (matched) {
                            if (EngineConstants._YES.equals(negation)) return false;
                            return true;
                        }
                    }
                }

                if (EngineConstants._YES.equals(negation)) return true;
                return isFound;
            }
        }

        // ----------------------------------------------------------------
        // REGEX — count matches
        // ----------------------------------------------------------------
        if (EngineConstants.REGEX.equals(searchMethod)) {

            if (isEmpty(searchIn) || isEmpty(searchTerm)) return false;

            searchTerm = searchTerm.trim().toLowerCase();
            searchIn   = StringHelper.stringSpacesRemover(searchIn).toLowerCase();

            if (isEmpty(searchCondition)) searchCondition = EngineConstants.GMI;

            Map<String, String> data = new HashMap<>();
            data.put("flags", searchCondition);
            data.put("CS", "N");

            boolean isFound = false;
            StringTokenizer stIn = new StringTokenizer(searchIn, EngineConstants.DELIMITATOR_COMMA);

            while (stIn.hasMoreTokens()) {
                String field = stIn.nextToken();
                log.debug("[Regex] field={}", field);

                if (EngineConstants.BODY.equals(field)) {
                    data.put("text", StringHelper.stringRemoveSpacesAddingLineBreaks(body));
                } else if (EngineConstants.SUBJECT.equals(field)) {
                    data.put("text", StringHelper.stringRemoveSpacesAddingLineBreaks(subject));
                }

                StringTokenizer stTerm = new StringTokenizer(searchTerm, EngineConstants.DELIMITATOR_SEMICOLON);
                while (stTerm.hasMoreTokens()) {
                    String term = stTerm.nextToken().trim();
                    data.put("pattern", term);

                    @SuppressWarnings("unchecked")
                    JSONObject json = new JSONObject(data);
                    String numMatches = StringHelper.regex(json);
                    int n = StringHelper.toInteger(numMatches);
                    log.debug("[Regex] pattern={} matches={}", term, n);

                    if (n > 0) {
                        if (EngineConstants._YES.equals(negation)) return false;
                        return true;
                    }
                }
            }

            if (EngineConstants._YES.equals(negation)) return true;
            return isFound;
        }

        // ----------------------------------------------------------------
        // REGEXFIRSTMATCH — extract first match into a column
        // ----------------------------------------------------------------
        if (EngineConstants.REGEXFIRSTMATCH.equals(searchMethod)) {

            if (isEmpty(searchCondition)) searchCondition = EngineConstants.GMI;

            Map<String, String> data = new HashMap<>();
            data.put("pattern", searchTerm);
            data.put("flags", searchCondition);
            data.put("CS", "N");

            boolean isFound = false;
            StringTokenizer stIn = new StringTokenizer(searchIn, EngineConstants.DELIMITATOR_COMMA);

            while (stIn.hasMoreTokens()) {
                String field = stIn.nextToken();

                if (EngineConstants.BODY.equals(field) || EngineConstants.SUBJECT.equals(field)) {
                    if (isFound) continue;

                    if (EngineConstants.BODY.equals(field)) {
                        data.put("text", StringHelper.stringRemoveSpacesAddingLineBreaks(body));
                    } else {
                        data.put("text", StringHelper.stringRemoveSpacesAddingLineBreaks(subject));
                    }

                    StringTokenizer stTerm = new StringTokenizer(searchTerm, EngineConstants.DELIMITATOR_SEMICOLON);
                    while (stTerm.hasMoreTokens() && !isFound) {
                        String term = stTerm.nextToken().trim();
                        data.put("pattern", term);

                        @SuppressWarnings("unchecked")
                        JSONObject json = new JSONObject(data);
                        String firstMatch = StringHelper.regexFirstMach(json);
                        log.debug("[RegexFirstMatch] pattern={} firstMatch={}", term, firstMatch);

                        if (firstMatch != null && !firstMatch.trim().isEmpty()) {
                            context.setColumnValue(updateColumn, firstMatch);
                            isFound = true;
                        }
                    }
                }
            }
        }

        // ----------------------------------------------------------------
        // SET — directly set a column value
        // ----------------------------------------------------------------
        if (EngineConstants.SET.equals(type)) {
            context.setColumnValue(updateColumn, updateValue);
            return true;
        }

        // ----------------------------------------------------------------
        // GET — read a column and compare its value
        // ----------------------------------------------------------------
        if (EngineConstants.GET.equals(type)) {
            String storedValue = context.getColumnValue(updateColumn);
            if (storedValue == null) return false;

            StringTokenizer stTerm = new StringTokenizer(searchTerm, EngineConstants.DELIMITATOR_SEMICOLON);

            if (EngineConstants.EQUALS.equals(searchCondition)) {
                while (stTerm.hasMoreTokens()) {
                    String term = stTerm.nextToken().trim();
                    if (term.equals(storedValue)) {
                        if (EngineConstants._YES.equals(negation)) return false;
                        return true;
                    }
                }
            } else if (EngineConstants.CONTAINS.equals(searchCondition)) {
                while (stTerm.hasMoreTokens()) {
                    String term = stTerm.nextToken().trim();
                    if (StringHelper.checkContains(storedValue, term)) {
                        if (EngineConstants._YES.equals(negation)) return false;
                        return true;
                    }
                }
            }
            return false;
        }

        // ----------------------------------------------------------------
        // IF — check if a value exists in memory
        // ----------------------------------------------------------------
        if (EngineConstants.IF.equals(type)) {
            if (isEmpty(searchTerm) || isEmpty(inMemory)) return false;

            String normalizedMemKey = StringHelper.stringSpacesRemover(inMemory).toLowerCase();

            for (Map.Entry<String, String> entry : memory.getMemoryTable().entrySet()) {
                String val = StringHelper.stringSpacesRemover(entry.getValue()).toLowerCase();
                if (val.equals(normalizedMemKey)) {
                    if (EngineConstants._YES.equals(searchTerm)) {
                        if (EngineConstants._YES.equals(negation)) return false;
                        return true;
                    }
                }
            }

            // Value not found in memory
            if (EngineConstants._NO.equals(searchTerm)) {
                if (EngineConstants._YES.equals(negation)) return false;
                return true;
            }
            return false;
        }

        // ----------------------------------------------------------------
        // DECLARATION — declare a variable in memory
        // ----------------------------------------------------------------
        if (EngineConstants.DECLARATION.equals(type)) {
            if (!isEmpty(wksName)) {
                memory.declareVariable(wksName, wksid, type, searchMethod, searchCondition, searchTerm);
                log.debug("[Declaration] {} = {}", wksName, searchTerm);
            }
            return true;
        }

        // ----------------------------------------------------------------
        // SET_VARIABLE — update a variable in memory
        // ----------------------------------------------------------------
        if (EngineConstants.SET_VARIABLE.equals(type)) {
            if (!isEmpty(wksName)) {
                memory.setVariable(searchMethod, wksid, type, searchMethod, searchCondition, searchTerm);
                log.debug("[SetVariable] {} = {}", searchMethod, searchTerm);
            }
            return true;
        }

        // ----------------------------------------------------------------
        // OPERATION — arithmetic on variables
        // ----------------------------------------------------------------
        if (EngineConstants.OPERATION.equals(type)) {
            if (EngineConstants.ARITHMETIC.equals(searchMethod)
                    && EngineConstants.ARITHMETIC_SUM.equals(searchCondition)) {

                StringTokenizer stTerm = new StringTokenizer(searchTerm, EngineConstants.DELIMITATOR_SEMICOLON);
                int total = 0;
                while (stTerm.hasMoreTokens()) {
                    String varName = StringHelper.stringSpacesRemover(stTerm.nextToken()).toLowerCase();
                    String varVal  = memory.getVariableValue(varName);
                    try { total += Integer.parseInt(varVal == null ? "0" : varVal); }
                    catch (NumberFormatException e) { /* skip */ }
                }
                memory.storeOperationResult(wksName, wksid, type, searchMethod, searchCondition, total + "");
                log.debug("[Operation/Sum] {} = {}", wksName, total);
            }
            return true;
        }

        // ----------------------------------------------------------------
        // CASE — evaluate variable against a condition expression
        // ----------------------------------------------------------------
        if (EngineConstants.CASE.equals(type)) {
            String varName = StringHelper.stringSpacesRemover(searchMethod).toLowerCase();

            for (MVPMemory.MVPVariable var : memory.getOperators().values()) {
                String name = StringHelper.stringSpacesRemover(var.getName()).toLowerCase();
                if (varName.equals(name)) {
                    String expression = var.getValue() + searchCondition + searchTerm;
                    log.debug("[Case] expression={}", expression);
                    try {
                        // Evaluate simple numeric expressions without Nashorn
                        boolean result = evaluateSimpleExpression(var.getValue(), searchCondition, searchTerm);
                        log.debug("[Case] result={}", result);
                        return result;
                    } catch (Exception e) {
                        log.warn("[Case] eval failed: {}", e.getMessage());
                        return false;
                    }
                }
            }
            return false;
        }

        // Default — unknown type → pass
        return true;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Get the string value of a named email field (Body/Subject/From/To/CC/BCC) */
    private String getFieldValue(String field) {
        if (field == null) return "";
        return switch (field.trim()) {
            case "Body"    -> body;
            case "Subject" -> subject;
            case "From"    -> from;
            case "To"      -> to;
            case "CC"      -> cc;
            case "BCC"     -> bcc;
            default        -> "";
        };
    }

    /** Evaluate a simple numeric comparison expression without Nashorn/JS engine */
    private boolean evaluateSimpleExpression(String leftVal, String operator, String rightVal) {
        try {
            double left  = Double.parseDouble(leftVal.trim());
            double right = Double.parseDouble(rightVal.trim());
            return switch (operator.trim()) {
                case ">"  -> left > right;
                case ">=" -> left >= right;
                case "<"  -> left < right;
                case "<=" -> left <= right;
                case "==" -> left == right;
                case "!=" -> left != right;
                default   -> false;
            };
        } catch (NumberFormatException e) {
            // Fall back to string comparison
            return switch (operator.trim()) {
                case "==" -> leftVal.trim().equals(rightVal.trim());
                case "!=" -> !leftVal.trim().equals(rightVal.trim());
                default   -> false;
            };
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
