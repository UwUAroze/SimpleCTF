package me.aroze.simplectf.listener;

import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

public final class PlayerInventoryListener implements Listener {

    @EventHandler
    public void onDrop(final PlayerDropItemEvent event) {
        final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(event.getPlayer());
        if (ctfPlayer.teamColor() == null) return;

        if (CTFGame.instance().gameState() == GameState.WAITING) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (CTFGame.instance().gameState() == GameState.WAITING) return;

        final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(player);
        if (ctfPlayer.teamColor() == null) return;

        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }

}
