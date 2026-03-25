package me.aroze.simplectf.task;

import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ticks the active game timer once per second while a match is in progress. Handles ending the game after
 * {@link #GAME_TIME_LIMIT_SECONDS} have elapsed, and updates the game boss bar with the remaining time and team scores.
 */
public final class GameTickTask extends BukkitRunnable {

    /** The amount of seconds the game runs for before automatically ending */
    private static final int GAME_TIME_LIMIT_SECONDS = 600;

    /** The remaining number of seconds in the game */
    private int remainingTimeSeconds = GAME_TIME_LIMIT_SECONDS;

    @Override
    public void run() {
        if (CTFGame.instance().gameState() != GameState.IN_PROGRESS) {
            return;
        }

        remainingTimeSeconds--;
        tick();

        if (remainingTimeSeconds <= 0) {
            CTFGame.instance().stop();
        }
    }

    public void tick() {
        CTFGame.instance().bossBar().name(buildBossBarTitle());
        CTFGame.instance().bossBar().progress(Math.max(0f, Math.min(1f, remainingTimeSeconds / (float) GAME_TIME_LIMIT_SECONDS)));
    }

    private String formatRemainingTime() {
        final int safeRemainingSeconds = Math.max(remainingTimeSeconds, 0);
        final int minutes = safeRemainingSeconds / 60;
        final int seconds = safeRemainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private Component buildBossBarTitle() {
        Component title = Component.text(Unicode.CLOCK, CtfMiniMessage.SECONDARY_COLOR)
            .appendSpace()
            .append(Component.text(formatRemainingTime(), CtfMiniMessage.PRIMARY_COLOR));

        for (final Team team : CTFGame.instance().getAllTeams()) {
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

