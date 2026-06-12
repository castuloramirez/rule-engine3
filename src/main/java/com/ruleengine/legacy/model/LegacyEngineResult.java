package com.ruleengine.legacy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * LegacyEngineResult — what the legacy rule engine produces after processing one email.
 *
 * Mirrors the data that the original engine writes back to Dataverse columns.
 * In this standalone version it's returned as part of the REST response instead.
 */
public class LegacyEngineResult {

    /** Final category assigned (cr22a_category) */
    private String category = "General";

    /** Category cluster (new_category_cluster) */
    private String categoryCluster = "";

    /** Final status (new_statusmvp) */
    private String status = "Open";

    /** Client assigned (e.g. UNIQA) */
    private String client = "UNIQA";

    /** Extracted claim number */
    private String claimNumber = "";

    /** Extracted policy number */
    private String policyNumber = "";

    /** Extracted branch */
    private String branch = "";

    /** SET actions that fired: "column=value" */
    private List<String> columnUpdates = new ArrayList<>();

    /** All workflow step IDs that returned TRUE */
    private List<String> passedSteps = new ArrayList<>();

    /** All workflow step IDs that returned FALSE (caused stop) */
    private List<String> stoppedAt = new ArrayList<>();

    /** Names of processes that ran */
    private List<String> processesRun = new ArrayList<>();

    /** Rule engine log messages (mirrors LogEntryMVP) */
    private List<String> log = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** No-args constructor — all fields initialised to their declared defaults. */
    public LegacyEngineResult() {}

    /** All-args constructor. */
    public LegacyEngineResult(
            String category,
            String categoryCluster,
            String status,
            String client,
            String claimNumber,
            String policyNumber,
            String branch,
            List<String> columnUpdates,
            List<String> passedSteps,
            List<String> stoppedAt,
            List<String> processesRun,
            List<String> log) {
        this.category       = category;
        this.categoryCluster = categoryCluster;
        this.status         = status;
        this.client         = client;
        this.claimNumber    = claimNumber;
        this.policyNumber   = policyNumber;
        this.branch         = branch;
        this.columnUpdates  = columnUpdates;
        this.passedSteps    = passedSteps;
        this.stoppedAt      = stoppedAt;
        this.processesRun   = processesRun;
        this.log            = log;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getCategory()             { return category; }
    public String getCategoryCluster()      { return categoryCluster; }
    public String getStatus()               { return status; }
    public String getClient()               { return client; }
    public String getClaimNumber()          { return claimNumber; }
    public String getPolicyNumber()         { return policyNumber; }
    public String getBranch()               { return branch; }
    public List<String> getColumnUpdates()  { return columnUpdates; }
    public List<String> getPassedSteps()    { return passedSteps; }
    public List<String> getStoppedAt()      { return stoppedAt; }
    public List<String> getProcessesRun()   { return processesRun; }
    public List<String> getLog()            { return log; }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setCategory(String category)                    { this.category = category; }
    public void setCategoryCluster(String categoryCluster)      { this.categoryCluster = categoryCluster; }
    public void setStatus(String status)                        { this.status = status; }
    public void setClient(String client)                        { this.client = client; }
    public void setClaimNumber(String claimNumber)              { this.claimNumber = claimNumber; }
    public void setPolicyNumber(String policyNumber)            { this.policyNumber = policyNumber; }
    public void setBranch(String branch)                        { this.branch = branch; }
    public void setColumnUpdates(List<String> columnUpdates)    { this.columnUpdates = columnUpdates; }
    public void setPassedSteps(List<String> passedSteps)        { this.passedSteps = passedSteps; }
    public void setStoppedAt(List<String> stoppedAt)            { this.stoppedAt = stoppedAt; }
    public void setProcessesRun(List<String> processesRun)      { this.processesRun = processesRun; }
    public void setLog(List<String> log)                        { this.log = log; }

    // -------------------------------------------------------------------------
    // Business logic
    // -------------------------------------------------------------------------

    /** Whether any rule set a category other than "General" */
    public boolean hasSpecificCategory() {
        return category != null && !category.equals("General");
    }
}