package com.ruleengine.legacy.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LegacyEngineContext — mutable state that the legacy engine reads and writes
 * during a processing run.
 *
 * Mirrors the instance variables in RuleEngine.java that get updated via
 * updateColumn() / statusHandler() / setCurrentEmailCategory():
 *
 *   currentEmailCategory    → category assigned so far
 *   statusMvp               → email status (Open, Closed, etc.)
 *   client                  → client name
 *   claimNumber             → extracted claim number
 *   policyNumber            → extracted policy number
 *   branch                  → extracted branch
 *   columnValues            → arbitrary column → value map (for GET/SET)
 *   processingLog           → processing log lines
 *   categories              → all categories assigned (deduped)
 */
public class LegacyEngineContext {

    private static final org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(LegacyEngineContext.class);

    // ── Fields ────────────────────────────────────────────────────────────────

    private String currentEmailCategory = "General";
    private String statusMvp            = "Open";
    private String client               = "UNIQA";
    private String claimNumber          = "";
    private String policyNumber         = "";
    private String branch               = "";

    private final Map<String, String> columnValues  = new HashMap<>();
    private final Map<String, String> categories    = new HashMap<>();
    private final List<String>        processingLog = new ArrayList<>();
    private final List<String>        columnUpdates = new ArrayList<>();

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getCurrentEmailCategory() { return currentEmailCategory; }
    public String getStatusMvp()            { return statusMvp; }
    public String getClient()               { return client; }
    public String getClaimNumber()          { return claimNumber; }
    public String getPolicyNumber()         { return policyNumber; }
    public String getBranch()               { return branch; }
    public Map<String, String> getColumnValues()  { return columnValues; }
    public Map<String, String> getCategories()    { return categories; }
    public List<String> getProcessingLog()        { return processingLog; }
    public List<String> getColumnUpdates()        { return columnUpdates; }

    /** Kept for backward compatibility with callers using getLog() */
    public List<String> getLog() { return new ArrayList<>(processingLog); }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setCurrentEmailCategory(String currentEmailCategory) {
        this.currentEmailCategory = currentEmailCategory;
    }

    public void setStatusMvp(String statusMvp) {
        this.statusMvp = statusMvp;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    // ── Business methods ──────────────────────────────────────────────────────

    /**
     * Set a column value — mirrors updateColumn() in RuleEngine.java.
     */
    public void setColumnValue(String column, String value) {
        if (column == null || column.trim().isEmpty()) return;
        if (value  == null || value.trim().isEmpty())  return;

        String colNorm = column.trim().toLowerCase().replaceAll("\\s+", "");
        columnValues.put(colNorm, value);
        columnUpdates.add(column + " = " + value);
        processingLog.add("SET " + column + " = " + value);

        // Route to specific fields (mirrors updateColumn special-case logic)
        switch (colNorm) {
            case "category" -> {
                if (!categories.containsKey(value)) {
                    categories.put(value, value);
                }
                currentEmailCategory = value;
                processingLog.add("Category set to: " + value);
            }
            case "statusmvp"    -> statusMvp    = value;
            case "client"       -> client        = value;
            case "claimnumber"  -> claimNumber   = value;
            case "policynumber" -> policyNumber  = value;
            case "branch"       -> branch        = value;
        }

        logger.debug("[Context] setColumnValue({}, {})", column, value);
    }

    /**
     * Get a column value — mirrors the GET type logic in RuleEngine.java.
     */
    public String getColumnValue(String column) {
        if (column == null) return null;
        String colNorm = column.trim().toLowerCase().replaceAll("\\s+", "");
        return columnValues.get(colNorm);
    }

    /**
     * Add a log message — mirrors logEntryMVP.setMessage()
     */
    public void addLog(String message) {
        processingLog.add(message);
    }

    /**
     * Whether any specific (non-General) category was assigned.
     */
    public boolean hasSpecificCategory() {
        return !"General".equals(currentEmailCategory);
    }

    // ── equals / hashCode / toString ─────────────────────────────────────────

    @Override
    public String toString() {
        return "LegacyEngineContext{" +
                "currentEmailCategory='" + currentEmailCategory + '\'' +
                ", statusMvp='" + statusMvp + '\'' +
                ", client='" + client + '\'' +
                ", claimNumber='" + claimNumber + '\'' +
                ", policyNumber='" + policyNumber + '\'' +
                ", branch='" + branch + '\'' +
                ", columnValues=" + columnValues +
                ", categories=" + categories +
                '}';
    }
}