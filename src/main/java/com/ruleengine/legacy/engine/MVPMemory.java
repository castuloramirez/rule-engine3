package com.ruleengine.legacy.engine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * MVPMemory — in-process memory store.
 *
 * Mirrors the two maps in the original RuleEngine:
 *   memoryTable_          → stores categories/values that have been SET
 *   memoryTableOperators_ → stores declared variables and operation results
 *
 * Used by:
 *   - IF type rules  (check if a value is in memoryTable_)
 *   - Declaration    (store a variable in memoryTableOperators_)
 *   - SetVariable    (update a variable in memoryTableOperators_)
 *   - Operation      (arithmetic on memoryTableOperators_ values)
 *   - Case           (evaluate expression against memoryTableOperators_ values)
 */
@Slf4j
public class MVPMemory {

    /**
     * memoryTable_ — stores values that have been SET during processing.
     * Key = value (e.g. the category name like "Invoice"), Value = same.
     * Used by IF-type rules to check if a category/value was already set.
     */
    private final Map<String, String> memoryTable = new HashMap<>();

    /**
     * memoryTableOperators_ — stores declared variables.
     * Key = variable name, Value = MVPVariable.
     */
    private final Map<String, MVPVariable> operators = new HashMap<>();

    // -----------------------------------------------------------------------
    // memoryTable operations (categories / SET values)
    // -----------------------------------------------------------------------

    public void putValue(String key, String value) {
        memoryTable.put(key.toLowerCase(), value);
        log.debug("[MVPMemory] SET value: {} = {}", key, value);
    }

    public boolean hasValue(String key) {
        String normalized = key == null ? "" : key.toLowerCase().trim();
        for (Map.Entry<String, String> entry : memoryTable.entrySet()) {
            if (entry.getValue().toLowerCase().trim().equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, String> getMemoryTable() {
        return memoryTable;
    }

    // -----------------------------------------------------------------------
    // memoryTableOperators_ operations (declared variables)
    // -----------------------------------------------------------------------

    public void declareVariable(String name, String id, String type, String container,
                                 String dataType, String value) {
        MVPVariable v = new MVPVariable(name, id, type, container, dataType, value);
        operators.put(name, v);
        log.debug("[MVPMemory] DECLARE variable: {} = {}", name, value);
    }

    public void setVariable(String name, String id, String type, String container,
                             String dataType, String value) {
        operators.remove(name);
        MVPVariable v = new MVPVariable(name, id, type, container, dataType, value);
        operators.put(name, v);
        log.debug("[MVPMemory] SET variable: {} = {}", name, value);
    }

    public String getVariableValue(String name) {
        String normalized = name == null ? "" : name.toLowerCase().trim();
        for (Map.Entry<String, MVPVariable> entry : operators.entrySet()) {
            if (entry.getKey().toLowerCase().trim().equals(normalized)) {
                return entry.getValue().getValue();
            }
        }
        return null;
    }

    public boolean hasVariable(String name) {
        return getVariableValue(name) != null;
    }

    public Map<String, MVPVariable> getOperators() {
        return operators;
    }

    public void storeOperationResult(String name, String id, String type,
                                      String container, String dataType, String value) {
        operators.put(name, new MVPVariable(name, id, type, container, dataType, value));
        log.debug("[MVPMemory] OPERATION result: {} = {}", name, value);
    }

    // -----------------------------------------------------------------------
    // Debug dump (mirrors printLOG in RuleEngine)
    // -----------------------------------------------------------------------
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MemoryTable ===\n");
        memoryTable.forEach((k, v) -> sb.append("  ").append(k).append(" = ").append(v).append("\n"));
        sb.append("=== MemoryTableOperators ===\n");
        operators.forEach((k, v) -> sb.append("  ").append(k).append(" = ").append(v.getValue()).append("\n"));
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Inner class: MVPVariable
    // -----------------------------------------------------------------------
    @Data
    public static class MVPVariable {
        private final String name;
        private final String id;
        private final String type;
        private final String container;
        private final String dataType;
        private       String value;

        public MVPVariable(String name, String id, String type,
                           String container, String dataType, String value) {
            this.name      = name;
            this.id        = id;
            this.type      = type;
            this.container = container;
            this.dataType  = dataType;
            this.value     = value;
        }
    }
}
