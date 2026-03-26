package me.aroze.simplectf.util.text;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

/**
 * A collection of {@link Component} manipulation utilities
 */
@Accessors(fluent = true)
@UtilityClass
public class ComponentUtil {

    /** A basic join configuration for comma separating and using "and" for the last (or only) element */
    @Getter
    private final JoinConfiguration JOIN_CONFIGURATION_COMMA_AND = JoinConfiguration.separators(Component.text(", "), Component.text(" and "));

}
