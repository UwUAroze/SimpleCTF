package me.aroze.simplectf.util.text;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * A collection of string manipulation utilities
 */
@UtilityClass
public class StringUtil {

    /**
     * Capitalizes the first character of a string
     *
     * @param input The string to capitalize
     * @return The capitalized string
     */
    public String capitalize(final @NotNull String input) {
        if (input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}
