package me.aroze.simplectf.listener;

import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

public final class ConnectionListener implements Listener {

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final @Nullable CTFPlayer ctfPlayer = PlayerManager.getInstance().removePlayer(event.getPlayer().getUniqueId());

        if (ctfPlayer != null) {
            CTFGame.instance().removePlayer(ctfPlayer);
        }
    }

}
