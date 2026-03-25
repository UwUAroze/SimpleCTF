package me.aroze.simplectf.util.text;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

/**
 * A collection of component manipulation utilities
 */
@Accessors(fluent = true)
public final class ComponentUtil {

    /** A basic join configuration for comma separating and using "and" for the last (or only) element */
    @Getter
    private static final JoinConfiguration JOIN_CONFIGURATION_COMMA_AND = JoinConfiguration.separators(Component.text(", "), Component.text(" and "));

    private ComponentUtil() {
    }

}
