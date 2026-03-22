package me.aroze.simplectf.team;

import lombok.AllArgsConstructor;
import org.bukkit.Material;

@AllArgsConstructor
public enum TeamColor {
    RED(Material.RED_BANNER),
    BLUE(Material.BLUE_BANNER),
    ;

    private final Material flagType;
}
