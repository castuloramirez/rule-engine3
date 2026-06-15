package com.ruleengine.legacy.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringHelper — mirrors your production StringHelper utility class.
 *
 * Contains:
 *   checkContains()                — case-insensitive contains
 *   checkContainsCaseSensitive()   — respects the caseSensitive flag
 *   stringSpacesRemover()          — removes all spaces
 *   stringRemoveSpacesAddingLineBreaks() — normalizes whitespace
 *   regex()                        — runs a regex and returns match count
 *   regexFirstMatch()              — returns first match string
 *   toInteger()                    — safe parse
 *   getName()                      — strips file extension
 *   getValidAttachments()          — filters attachment names by pattern
 */

public class StringHelper {

    private static final Logger log = LogManager.getLogger(StringHelper.class);
    /**
     * Case-insensitive contains check.
     * Mirrors: StringHelper.checkContains(text, term)
     */
    public static boolean checkContains(String text, String term) {
        if (text == null || term == null) return false;
        return text.toLowerCase().contains(term.toLowerCase());
    }

    /**
     * Contains check respecting caseSensitive flag.
     * Mirrors: StringHelper.checkContainsCaseSensitive(text, term, caseSensitive)
     *
     * @param caseSensitive "1" = case-sensitive, anything else = case-insensitive
     */
    public static boolean checkContainsCaseSensitive(String text, String term, String caseSensitive) {
        if (text == null || term == null) return false;
        if ("1".equals(caseSensitive)) {
            return text.contains(term);
        }
        return text.toLowerCase().contains(term.toLowerCase());
    }

    /**
     * Removes ALL whitespace from a string.
     * Mirrors: StringHelper.stringSpacesRemover(input)
     */
    public static String stringSpacesRemover(String input) {
        if (input == null) return "";
        return input.replaceAll("\\s+", "");
    }

    /**
     * Replaces multiple spaces with newlines for regex processing.
     * Mirrors: StringHelper.stringRemoveSpacesAddingLineBreaks(input)
     */
    public static String stringRemoveSpacesAddingLineBreaks(String input) {
        if (input == null) return "";
        return input.replaceAll("[ \\t]+", "\n").trim();
    }

    /**
     * Runs a regex pattern against text and returns the match count as String.
     * Mirrors: StringHelper.regex(jsonObject) — which previously called an Azure Function.
     *
     * JSON input fields: pattern, text, flags, CS
     * Returns: number of matches as string (e.g. "3" or "0")
     */
    public static String regex(org.json.simple.JSONObject json) {
        try {
            String pattern = (String) json.get("pattern");
            String text    = (String) json.get("text");
            String flags   = (String) json.get("flags");

            if (pattern == null || text == null) return "0";

            int regexFlags = buildFlags(flags);
            Pattern p = Pattern.compile(pattern, regexFlags);
            Matcher m = p.matcher(text);

            int count = 0;
            while (m.find()) count++;
            return count + "";
        } catch (Exception e) {
            log.warn("regex() failed: {}", e.getMessage());
            return "0";
        }
    }

    /**
     * Runs a regex pattern and returns the FIRST match string.
     * Mirrors: StringHelper.regexFirstMach(jsonObject)
     *
     * Returns: first match string, or "" if no match.
     */
    public static String regexFirstMach(org.json.simple.JSONObject json) {
        try {
            String pattern = (String) json.get("pattern");
            String text    = (String) json.get("text");
            String flags   = (String) json.get("flags");

            if (pattern == null || text == null) return "";

            int regexFlags = buildFlags(flags);
            Pattern p = Pattern.compile(pattern, regexFlags);
            Matcher m = p.matcher(text);

            if (m.find()) return m.group();
            return "";
        } catch (Exception e) {
            log.warn("regexFirstMach() failed: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe integer parse — returns 0 on failure.
     * Mirrors: StringHelper.toInteger(value)
     */
    public static int toInteger(String value) {
        try {
            return Integer.parseInt((value + "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Strips file extension from filename.
     * Mirrors: StringHelper.getName(filename)
     * e.g. "invoice.pdf" → "invoice"
     */
    public static String getName(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    /**
     * Filters attachment names by regex pattern, returns semicolon-separated valid names.
     * Mirrors: StringHelper.getValidAttachments(pattern, attachmentsName)
     */
    public static String getValidAttachments(String pattern, String attachmentsName) {
        if (pattern == null || attachmentsName == null) return "";
        try {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            StringBuilder sb = new StringBuilder();
            for (String name : attachmentsName.split(";")) {
                name = name.trim();
                if (!name.isEmpty() && p.matcher(name).find()) {
                    if (sb.length() > 0) sb.append(";");
                    sb.append(name);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("getValidAttachments() failed: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Returns empty string if value is "null" or blank.
     */
    public static String validateStringContentNull(String value) {
        if (value == null || "null".equals(value) || value.trim().isEmpty()) return "";
        return value;
    }

    /**
     * Basic date-time format converter (stub — add real logic if needed).
     */
    public static String convertDateTimeFormat(String dateTime) {
        if (dateTime == null || "null".equals(dateTime)) return "";
        return dateTime;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static int buildFlags(String flags) {
        if (flags == null) return Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
        int f = 0;
        String lower = flags.toLowerCase();
        if (lower.contains("i")) f |= Pattern.CASE_INSENSITIVE;
        if (lower.contains("m")) f |= Pattern.MULTILINE;
        if (lower.contains("s")) f |= Pattern.DOTALL;
        return f == 0 ? Pattern.CASE_INSENSITIVE | Pattern.MULTILINE : f;
    }
}
