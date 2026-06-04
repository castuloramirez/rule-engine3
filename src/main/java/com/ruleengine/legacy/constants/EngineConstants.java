package com.ruleengine.legacy.constants;

/**
 * EngineConstants — mirrors your existing production EngineConstants.
 *
 * These are the exact same constant names used in the original RuleEngine.java.
 * This allows the legacy search/matching logic to work without modification.
 */
public class EngineConstants {

    // -----------------------------------------------------------------------
    // Search Methods
    // -----------------------------------------------------------------------
    public static final String SEARCHWORD       = "SearchWord";
    public static final String REGEX            = "Regex";
    public static final String REGEXFIRSTMATCH  = "RegexFirstMatch";

    // -----------------------------------------------------------------------
    // Search Conditions
    // -----------------------------------------------------------------------
    public static final String CONTAINS         = "Contains";
    public static final String EQUALS           = "Equals";
    public static final String GMI              = "gmi";

    // -----------------------------------------------------------------------
    // Search In (fields to search)
    // -----------------------------------------------------------------------
    public static final String BODY             = "Body";
    public static final String SUBJECT          = "Subject";
    public static final String FROM             = "From";
    public static final String TO               = "To";
    public static final String CC               = "CC";
    public static final String BCC              = "BCC";
    public static final String ATTACHMENTS      = "Attachments";

    // -----------------------------------------------------------------------
    // Rule Types
    // -----------------------------------------------------------------------
    public static final String SET              = "Set";
    public static final String GET              = "Get";
    public static final String IF               = "If";
    public static final String DECLARATION      = "Declaration";
    public static final String SET_VARIABLE     = "SetVariable";
    public static final String OPERATION        = "Operation";
    public static final String CASE             = "Case";
    public static final String PLACEHOLDER      = "Placeholder";
    public static final String COLUMNEXPRESSION = "ColumnExpression";
    public static final String SENDEMAIL        = "SendEmail";
    public static final String ATTACH           = "Attach";
    public static final String INCLUSION_WORDS  = "InclusionWords";
    public static final String EXCLUSION_WORDS  = "ExclusionWords";
    public static final String GETSET           = "GetSet";
    public static final String ARITHMETIC       = "Arithmetic";
    public static final String ARITHMETIC_SUM   = "Sum";

    // -----------------------------------------------------------------------
    // Negation / Yes / No flags
    // -----------------------------------------------------------------------
    public static final String _YES             = "1";
    public static final String _NO              = "0";
    public static final String YES              = "YES";

    // -----------------------------------------------------------------------
    // Delimiters (same as production)
    // -----------------------------------------------------------------------
    public static final String DELIMITATOR_SEMICOLON = ";";
    public static final String DELIMITATOR_COMMA     = ",";

    // -----------------------------------------------------------------------
    // Category names (same as your production categories)
    // -----------------------------------------------------------------------
    public static final String CATEGORY              = "Category";
    public static final String CATEGORY_GENERAL      = "General";
    public static final String CATEGORY_EBF          = "EBF";
    public static final String CategoryCluster       = "CategoryCluster";

    // -----------------------------------------------------------------------
    // Status names
    // -----------------------------------------------------------------------
    public static final String STATUS_MVP            = "StatusMVP";
    public static final String STATUS_OPEN           = "Open";

    // -----------------------------------------------------------------------
    // Client
    // -----------------------------------------------------------------------
    public static final String CLIENT                = "Client";
    public static final String CLIENT_UNIQA          = "UNIQA";

    // -----------------------------------------------------------------------
    // Field names
    // -----------------------------------------------------------------------
    public static final String CLAIM_NUMBER          = "Claim Number";
    public static final String POLICY_NUMBER         = "Policy Number";
    public static final String BRANCH                = "Branch";
    public static final String FIRSTNAME             = "Firstname";
    public static final String LASTNAME              = "Lastname";
    public static final String MAILTYPE              = "MailType";

    // -----------------------------------------------------------------------
    // Attachment constants
    // -----------------------------------------------------------------------
    public static final int    ATTACH_NAME_LENGTH_VALUE = 100;
    public static final String ATTACH_NAME_LENGTH    = "Attachment name exceeds {0} characters";
    public static final String ATTACH_SIZES          = "No attachment sizes found";
    public static final String ATTACH_NO_COMPLAINTS  = "Attachment validation not configured";
    public static final String ATTACH_IGNORING_ATTACHS = "All attachments are below size threshold";
    public static final String ATTACH_NAMES_NO       = "No attachment names found";

    // -----------------------------------------------------------------------
    // Exception messages
    // -----------------------------------------------------------------------
    public static final String EXCEPTION_STOP_MSG               = "StopProcess:{0}:{1}";
    public static final String EXCEPTION_STOP_MSG_E             = "StopByExclusionWords";
    public static final String EXCEPTION_STOP_MSG_I             = "StopByInclusionWords";
    public static final String EXCEPTION_STOP_DUPLICATED_WITH_EXT    = "DuplicatedAttachmentWithExtension";
    public static final String EXCEPTION_STOP_DUPLICATED_WITHOUT_EXT = "DuplicatedAttachmentWithoutExtension";
    public static final String STARTING_CONDITIONS_MSG_NO       = "NoStartingConditions";

    // -----------------------------------------------------------------------
    // Misc
    // -----------------------------------------------------------------------
    public static final String CHARSET                   = "UTF-8";
    public static final String PREFIX_TEMPLATES          = "MVP-";
    public static final String PREFIX_TEMPLATES_NOT_FOUND = "Template not found";
}
