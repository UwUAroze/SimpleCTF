package me.aroze.simplectf.listener;

import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.util.PlayerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

public final class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        PlayerUtil.reset(event.getPlayer());

        if (CTFGame.instance().gameState() == GameState.IN_PROGRESS) {
            event.getPlayer().showBossBar(CTFGame.instance().bossBar());
        } else {
            event.getPlayer().hideBossBar(CTFGame.instance().bossBar());
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final @Nullable CTFPlayer ctfPlayer = PlayerManager.getInstance().removePlayer(event.getPlayer().getUniqueId());

        if (ctfPlayer != null) {
            CTFGame.instance().removePlayer(ctfPlayer);
        }
    }

}
