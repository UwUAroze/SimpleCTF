package me.aroze.simplectf.task;

import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.util.PlayerUtil;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;

/**
 * A task to respawn the player with a preceding death/respawning sequence.
 *
 * @see CTFGame#respawnPlayer(Player, CTFPlayer) to instantly respawn the player.
 */
public final class RespawnTask extends BukkitRunnable {

    private static final int RESPAWN_TICKS = 60;

    private final CTFGame game;
    private final Player player;
    private final CTFPlayer ctfPlayer;

    private int ticksElapsed = 0;

    public RespawnTask(final CTFGame game, final Player player) {
        this.game = game;
        this.player = player;
        this.ctfPlayer = PlayerManager.getInstance().getPlayer(player);
    }

    @Override
    public void run() {
        this.ticksElapsed++;

        if (!this.player.isValid()) {
            this.ctfPlayer.isRespawning(false);
            this.cancel();
            return;
        }

        this.player.setAllowFlight(true);
        this.player.setFlying(true);

        this.player.showTitle(Title.title(
            CtfMiniMessage.getInstance().deserialize("<#ff6378>☠ You died!"),
            CtfMiniMessage.getInstance().deserialize("<#e3bac0>Respawning in <#ffb3bf><seconds> seconds...",
                Placeholder.unparsed("seconds", String.format("%.1f", (RESPAWN_TICKS - this.ticksElapsed) / 20.0))
            ),
            Title.Times.times(
                Duration.ZERO,
                Duration.ofMillis(250),
                Duration.ZERO
            )
        ));

        if (this.ticksElapsed >= RESPAWN_TICKS) {
            this.game.respawnPlayer(this.player, this.ctfPlayer);
            this.ctfPlayer.isRespawning(false);
            this.cancel();
        }
    }

    /**
     * Starts the respawn sequence for the player
     */
    public void start() {
        if (this.ctfPlayer.isRespawning()) {
            return;
        }

        PlayerUtil.reset(this.player);
        this.ctfPlayer.isRespawning(true);
        this.player.setGameMode(GameMode.SPECTATOR);

        this.player.playSound(this.player, Sound.ENTITY_ALLAY_DEATH, 1f, 1f);
        this.player.playSound(this.player, Sound.ENTITY_ALLAY_DEATH, 1f, 2f);

        this.player.addPotionEffects(List.of(
            new PotionEffect(PotionEffectType.BLINDNESS, 55, 0),
            new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 0)
        ));

        this.player.setFlySpeed(0.0f);
        this.player.setAllowFlight(true);
        this.player.setFlying(true);
    }
}