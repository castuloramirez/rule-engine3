package com.ruleengine.legacy.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * LegacyEngineResult — what the legacy rule engine produces after processing one email.
 *
 * Mirrors the data that the original engine writes back to Dataverse columns.
 * In this standalone version it's returned as part of the REST response instead.
 */
@Data
@Builder
public class LegacyEngineResult {

    /** Final category assigned (cr22a_category) */
    @Builder.Default
    private String category = "General";

    /** Category cluster (new_category_cluster) */
    @Builder.Default
    private String categoryCluster = "";

    /** Final status (new_statusmvp) */
    @Builder.Default
    private String status = "Open";

    /** Client assigned (e.g. UNIQA) */
    @Builder.Default
    private String client = "UNIQA";

    /** Extracted claim number */
    @Builder.Default
    private String claimNumber = "";

    /** Extracted policy number */
    @Builder.Default
    private String policyNumber = "";

    /** Extracted branch */
    @Builder.Default
    private String branch = "";

    /** SET actions that fired: "column=value" */
    @Builder.Default
    private List<String> columnUpdates = new ArrayList<>();

    /** All workflow step IDs that returned TRUE */
    @Builder.Default
    private List<String> passedSteps = new ArrayList<>();

    /** All workflow step IDs that returned FALSE (caused stop) */
    @Builder.Default
    private List<String> stoppedAt = new ArrayList<>();

    /** Names of processes that ran */
    @Builder.Default
    private List<String> processesRun = new ArrayList<>();

    /** Rule engine log messages (mirrors LogEntryMVP) */
    @Builder.Default
    private List<String> log = new ArrayList<>();

    /** Whether any rule set a category other than "General" */
    public boolean hasSpecificCategory() {
        return category != null && !category.equals("General");
    }
}
