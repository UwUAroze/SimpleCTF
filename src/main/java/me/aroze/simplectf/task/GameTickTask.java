package me.aroze.simplectf.task;

import lombok.RequiredArgsConstructor;
import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ticks the active game timer once per second while a match is in progress. Handles ending the game after
 * {@link #GAME_TIME_LIMIT_SECONDS} have elapsed, and updates the game boss bar with the remaining time and team scores.
 */
@RequiredArgsConstructor
public final class GameTickTask extends BukkitRunnable {

    /** The amount of seconds the game runs for before automatically ending */
    private static final int GAME_TIME_LIMIT_SECONDS = 600;

    private final CTFGame game;

    /** The remaining number of seconds in the game */
    private int remainingTimeSeconds = GAME_TIME_LIMIT_SECONDS;

    private static final Component GAME_TIMED_OUT_MESSAGE = CtfMiniMessage.getInstance().deserialize("<s><clock> <p>The game is ending as the time limit has been reached!",
        Placeholder.unparsed("clock", Unicode.CLOCK)
    );

    @Override
    public void run() {
        if (this.game.gameState() != GameState.IN_PROGRESS) {
            return;
        }

        this.remainingTimeSeconds--;
        this.updateVisuals();

        if (this.remainingTimeSeconds <= 0) {
            Bukkit.broadcast(GAME_TIMED_OUT_MESSAGE);
            this.game.stop();
        }
    }

    /**
     * Instantly updates visuals (bossbar) to be upto date with info from this game tick
     */
    public void updateVisuals() {
        this.game.bossBar().name(this.buildBossBarTitle());
        this.game.bossBar().progress(Math.max(0f, Math.min(1f, this.remainingTimeSeconds / (float) GAME_TIME_LIMIT_SECONDS)));
    }

    private String formatRemainingTime() {
        final int safeRemainingSeconds = Math.max(this.remainingTimeSeconds, 0);
        final int minutes = safeRemainingSeconds / 60;
        final int seconds = safeRemainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private Component buildBossBarTitle() {
        Component title = Component.text(Unicode.CLOCK, CtfMiniMessage.SECONDARY_COLOR)
            .appendSpace()
            .append(Component.text(this.formatRemainingTime(), CtfMiniMessage.PRIMARY_COLOR));

        for (final Team team : this.game.getAllTeams()) {
            title = title.append(Component.text(" | ", CtfMiniMessage.SECONDARY_COLOR));
            title = title.append(Component.text(Unicode.FLAG, team.color().color()));
            title = title.append(Component.text(" "));

            for (int i = 0; i < CTFGame.WINNING_SCORE; i++) {
                final TextColor circleColor = i < team.score()
                    ? team.color().color()
                    : NamedTextColor.GRAY;

                title = title.append(Component.text(Unicode.CIRCLE, circleColor));
            }
        }

        return title;
    }
}

