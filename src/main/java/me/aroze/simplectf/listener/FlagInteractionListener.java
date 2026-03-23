package me.aroze.simplectf.listener;

import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.team.Team;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.Nullable;

public final class FlagInteractionListener implements Listener {

    @EventHandler
    public void onFlagClick(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof final Player player)) return;
        if (!(event.getEntity() instanceof final Interaction interaction)) return;
        if (CTFGame.instance().gameState() != GameState.IN_PROGRESS) return;

        final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(player);
        final @Nullable Team playerTeam = CTFGame.instance().getTeam(ctfPlayer);

        if (playerTeam == null) return;

        final @Nullable Team interactedTeam = CTFGame.instance().getAllTeams().stream()
            .filter(team -> team.flagInteractionUUID() != null && team.flagInteractionUUID().equals(interaction.getUniqueId()))
            .findFirst()
            .orElse(null);

        if (interactedTeam == null) return;
        if (playerTeam == interactedTeam) return;

        interactedTeam.destroyFlag(player);
    }

}
