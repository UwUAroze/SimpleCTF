package me.aroze.simplectf.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

/**
 * A collection of utilities to manipulate Bukkit {@link Player}s
 */
@UtilityClass
public class PlayerUtil {

    /**
     * "Resets" the player's general vanilla state, clearing their inventory and resetting potion effects, health, etc.
     *
     * @param player the {@link Player} to reset
     */
    public void reset(final Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(0.0f);
        player.setAbsorptionAmount(0.0);
        player.setFireTicks(0);
        player.setFreezeTicks(0);
        player.setArrowsInBody(0);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.getInventory().clear();
        player.clearActivePotionEffects();
    }

}
