package me.aroze.simplectf.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public final class PlayerHungerListener implements Listener {

    @EventHandler
    public void onHunger(final FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;
        if (event.getFoodLevel() == 20) return;

        player.setFoodLevel(20);
        event.setCancelled(true);
    }

}
