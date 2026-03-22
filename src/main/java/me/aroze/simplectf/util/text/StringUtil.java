package me.aroze.simplectf.util.text;

import org.jetbrains.annotations.NotNull;

/**
 * A collection of string manipulation utilities
 */
public final class StringUtil {

    private StringUtil() {
    }

    /**
     * Capitalizes the first character of a string
     *
     * @param input The string to capitalize
     * @return The capitalized string
     */
    public static String capitalize(final @NotNull String input) {
        if (input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}
