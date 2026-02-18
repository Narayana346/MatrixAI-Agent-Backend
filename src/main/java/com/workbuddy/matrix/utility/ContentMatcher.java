package com.workbuddy.matrix.utility;

import java.util.regex.Pattern;

public final class ContentMatcher {

    private static final String PATH_REGEX = "path=\"([^\"]+)\"";
    private static final String FILE_TAG_REGEX = "<file\\s+"+ PATH_REGEX +">(.*?)</file>";

    //matchers
    public static Pattern FILE_TAG_PATTERN = Pattern.compile(FILE_TAG_REGEX, Pattern.DOTALL);
    public static Pattern PATH_PATTERN = Pattern.compile(PATH_REGEX);


    /**
     * Regex Breakdown:
     * Group 1: Opening Tag (<tag ...>)
     * Group 2: Tag Name (message|file|tool)
     * Group 3: Attributes part (e.g., ' path="foo"' or ' args="a,b"')
     * Group 4: Content (The stuff inside)
     * Group 5: Closing Tag (</tag>)
     */
    public static final Pattern GENERIC_TAG_PATTERN = Pattern.compile(
            "(<(message|file|tool)([^>]*)>)([\\s\\S]*?)(</\\2>)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Helper to extract specific attributes (path="..." or args="...") from Group 3
    public static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "(path|args)=\"([^\"]+)\""
    );
}
