package com.workbuddy.matrix.utility;

import java.util.regex.Pattern;

public final class ContentMatcher {

    private static final String PATH_REGEX = "path=\"([^\"]+)\"";
    private static final String FILE_TAG_REGEX = "<file\\s+"+ PATH_REGEX +">(.*?)</file>";

    //matchers
    public static Pattern FILE_TAG_PATTERN = Pattern.compile(FILE_TAG_REGEX, Pattern.DOTALL);
    public static Pattern PATH_PATTERN = Pattern.compile(PATH_REGEX);
}
