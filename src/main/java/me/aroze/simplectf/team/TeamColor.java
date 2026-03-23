package me.aroze.simplectf.team;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.StringUtil;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Color;
import org.bukkit.Material;

/**
 * Represents metadata for a team color
 */
@Getter
@Accessors(fluent = true)
public enum TeamColor {
    RED(Material.RED_BANNER, TextColor.color(0xFFA1A1)),
    BLUE(Material.LIGHT_BLUE_BANNER, TextColor.color(0xA1D8FF)),
    ;

    /** The type of banner used for the team's physical flag */
    private final Material flagType;

    /** The color for the team, used for color matching text & armor where applicable */
    private final TextColor color;

    /** The Bukkit {@link Color} equivalent of the defined team color */
    private final Color bukkitColor;

    /** The display name of the team used in messages */
    private final String displayName;

    /** A formatted & colored display of the team name */
    private final Component formattedDisplayName;

    /** The team's color-coded kit */
    private final TeamKit kit;

    TeamColor(final Material flagType, final TextColor color) {
        this.flagType = flagType;
        this.color = color;
        this.bukkitColor = Color.fromRGB(color.value());

        this.displayName = StringUtil.capitalize(this.name().toLowerCase());
        this.formattedDisplayName = CtfMiniMessage.getInstance().deserialize(
            "<color><icon> <name> Team</color>",
            Placeholder.styling("color", color),
            Placeholder.unparsed("icon", Unicode.FLAG),
            Placeholder.unparsed("name", displayName)
        );

        this.kit = new TeamKit(this);
    }
}
