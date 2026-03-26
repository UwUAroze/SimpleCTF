package me.aroze.simplectf.listener;

import com.google.auto.service.AutoService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

@AutoService(Listener.class)
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
