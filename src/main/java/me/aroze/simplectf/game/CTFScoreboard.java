package me.aroze.simplectf.game;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the scoreboard teams used for coloring player names according to their team.
 */
@Accessors(fluent = true)
public final class CTFScoreboard {

    /** Singleton instance of the CTFScoreboard */
    @Getter
    private static final CTFScoreboard instance = new CTFScoreboard();

    private CTFScoreboard() {
    }

    /**
     * Gets or creates the {@link Team} for the specified {@link TeamColor}
     *
     * @param teamColor the team color to get the team for
     * @return the Bukkit team associated with the specified team color
     */
    public Team getCTFTeam(final TeamColor teamColor) {
        final Team team = this.getOrCreateTeam(teamColor.name());
        team.prefix(null);
        team.suffix(null);
        team.color(teamColor.namedTextColor());
        return team;
    }

    /**
     * Unregisters all tracked teams from the server
     */
    public void unregisterTeams() {
        for (final TeamColor teamColor: TeamColor.values()) {
            final @Nullable Team team = this.getScoreboard().getTeam(teamColor.name());
            if (team == null) {
                continue;
            }

            team.unregister();
        }
    }

    private Scoreboard getScoreboard() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    private Team getOrCreateTeam(final String name) {
        final Scoreboard scoreboard = this.getScoreboard();
        final @Nullable Team team = scoreboard.getTeam(name);
        if (team != null) {
            return team;
        }

        return scoreboard.registerNewTeam(name);
    }

}
