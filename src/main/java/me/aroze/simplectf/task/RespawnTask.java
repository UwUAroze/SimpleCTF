package me.aroze.simplectf.task;

import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.team.TeamColor;
import me.aroze.simplectf.util.PlayerUtil;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public final class RespawnTask extends BukkitRunnable {

    private static final int RESPAWN_TICKS = 60;

    private final Player player;
    private final CTFPlayer ctfPlayer;

    private int ticksElapsed = 0;

    public RespawnTask(final Player player) {
        this.player = player;
        ctfPlayer = PlayerManager.getInstance().getPlayer(player);

        if (ctfPlayer.isRespawning()) {
            return;
        }

        PlayerUtil.reset(player);
        ctfPlayer.isRespawning(true);
        player.setGameMode(GameMode.SPECTATOR);

        player.playSound(player, Sound.ENTITY_ALLAY_DEATH, 1f, 1f);
        player.playSound(player, Sound.ENTITY_ALLAY_DEATH, 1f, 2f);

        player.addPotionEffects(List.of(
            new PotionEffect(PotionEffectType.BLINDNESS, 55, 0),
            new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 0)
        ));

        player.setFlySpeed(0.0f);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    @Override
    public void run() {
        ticksElapsed++;

        if (!player.isValid()) {
            ctfPlayer.isRespawning(false);
            cancel();
            return;
        }

        player.setAllowFlight(true);
        player.setFlying(true);

        player.showTitle(Title.title(
            CtfMiniMessage.getInstance().deserialize("<#ff6378>☠ You died!"),
            CtfMiniMessage.getInstance().deserialize("<#e3bac0>Respawning in <#ffb3bf><seconds> seconds...",
                Placeholder.unparsed("seconds", String.format("%.1f", (RESPAWN_TICKS - ticksElapsed) / 20.0))
            ),
            Title.Times.times(
                Duration.ZERO,
                Duration.ofMillis(250),
                Duration.ZERO
            )
        ));

        if (ticksElapsed >= RESPAWN_TICKS) {
            respawnPlayer(player, ctfPlayer);
            ctfPlayer.isRespawning(false);
            cancel();
        }
    }

    public static void respawnPlayer(final Player player, final CTFPlayer ctfPlayer) {
        player.clearTitle();
        player.teleport(getRespawnLocation(ctfPlayer));
        player.setGameMode(GameMode.SURVIVAL);

        final @Nullable TeamColor teamColor = ctfPlayer.teamColor();
        if (teamColor != null) {
            teamColor.kit().applyKit(player);
        }
    }

    private static Location getRespawnLocation(final CTFPlayer player) {
        final @Nullable TeamColor teamColor = player.teamColor();
        if (teamColor == null) {
            return player.bukkitPlayer().getWorld().getSpawnLocation(); // Fallback, e.g. player left the game while respawning
        }

        final Team team = CTFGame.instance().getTeam(teamColor);
        return team.baseLocation() == null
            ? player.bukkitPlayer().getWorld().getSpawnLocation() // Shouldn't happen!
            : team.baseLocation();
    }
}