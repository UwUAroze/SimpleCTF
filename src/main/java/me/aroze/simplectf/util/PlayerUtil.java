package me.aroze.simplectf.util;

import org.bukkit.entity.Player;

public final class PlayerUtil {

    public static void reset(final Player player) {
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
    }
}
