package me.aroze.simplectf.listener;

import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.team.FlagRetrievalType;
import me.aroze.simplectf.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class FlagCaptureListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(final PlayerMoveEvent event) {
        if (CTFGame.instance().gameState() != GameState.IN_PROGRESS) return;
        if (!event.hasChangedPosition()) return;

        final Player player = event.getPlayer();
        final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(player);

        if (ctfPlayer.teamColor() == null) return;
        if (ctfPlayer.carryingFlag() == null) return;

        final Team playerTeam = CTFGame.instance().getTeam(ctfPlayer.teamColor());
        if (!playerTeam.isInBase(player.getLocation())) return;

        final Team otherTeam = CTFGame.instance().getTeam(ctfPlayer.carryingFlag());

        otherTeam.retrieveFlag(FlagRetrievalType.CAPTURED, player);
        playerTeam.incrementScore();
    }

}
