package com.example.aidigest.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w\\s-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-+|-+$)");
    private static final int MAX_LENGTH = 80;

    private SlugUtil() {}

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String stripped = NON_LATIN.matcher(normalized).replaceAll("");
        String hyphenated = WHITESPACE.matcher(stripped).replaceAll("-");
        String lower = hyphenated.toLowerCase(Locale.ROOT);
        String trimmed = EDGE_DASHES.matcher(lower).replaceAll("");
        if (trimmed.length() > MAX_LENGTH) {
            trimmed = trimmed.substring(0, MAX_LENGTH);
            trimmed = EDGE_DASHES.matcher(trimmed).replaceAll("");
        }
        return trimmed;
    }
}
