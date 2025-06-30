package edu.charlotte.parser.utils;

public final class ParserUtils {

    private static final int DEFAULT_TRUNCATION_LENGTH = 50;
    private ParserUtils() {}

    public static String formatInputForLogging(String input, int maxLength) {
        if (input == null)
            return "[null]";

        if (maxLength < 0) {
            maxLength = 0;
        }

        if (input.length() > maxLength) {
            int effectiveLength = Math.max(0, maxLength - 3); // Leave room for "..."
            return input.substring(0, effectiveLength) + "...";
        } else
            return input;
    }

    public static String formatInputForLogging(String input) {
        return formatInputForLogging(input, ParserUtils.DEFAULT_TRUNCATION_LENGTH);
    }
}