package me.aroze.simplectf.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockProtectionListener implements Listener {

    @EventHandler
    public void onBreak(final BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(final BlockPlaceEvent event) {
        event.setCancelled(true);
    }

}
