package me.aroze.simplectf.game;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.team.TeamColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents an ongoing Capture The Flag game, providing methods for retrieving and modifying game state.
 */
@Accessors(fluent = true)
public final class CTFGame {

    /** Singleton instance of the CTFGame */
    @Getter
    private static final CTFGame instance = new CTFGame();

    private final Map<TeamColor, Team> teams = new EnumMap<>(TeamColor.class);

    private CTFGame() {
        for (TeamColor teamColor : TeamColor.values()) {
            teams.put(teamColor, new Team());
        }
    }

    /**
     * Retrieves {@link Team} info by {@link TeamColor}.
     *
     * @param teamColor the {@link TeamColor} for which to retrieve the team
     * @return the associated {@link Team} instance
     */
    @NotNull
    public Team getTeam(final TeamColor teamColor) {
        return teams.get(teamColor);
    }

    /**
     * Retrieves {@link Team} info by checking what team {@link CTFPlayer} belongs to, may be {@code null} if the player is not in a team
     *
     * @param ctfPlayer the {@link CTFPlayer} for which to retrieve the team
     * @return the {@link Team} instance or {@code null} if the player is not on any team
     */
    @Nullable
    public Team getTeam(final @NotNull CTFPlayer ctfPlayer) {
        final @Nullable TeamColor color = ctfPlayer.teamColor();
        if (color == null) {
            return null;
        }

        return teams.get(color);
    }

    /**
     * Sets the team for a {@link CTFPlayer}. If the player is already on a team, they will be removed from that team before being added to the new one.
     *
     * @param ctfPlayer the {@link CTFPlayer} for which to set the team
     * @param teamColor the {@link TeamColor} to set the player to
     */
    public void setTeam(final @NotNull CTFPlayer ctfPlayer, final @NotNull TeamColor teamColor) {
        final Team currentTeam = getTeam(ctfPlayer);

        if (currentTeam != null) {
            currentTeam.members().remove(ctfPlayer.uuid());
        }

        final Team newTeam = teams.get(teamColor);
        newTeam.members().add(ctfPlayer.uuid());

        ctfPlayer.teamColor(teamColor);
    }

    /**
     * Removes the given {@link CTFPlayer} from the game
     *
     * @param ctfPlayer the {@link CTFPlayer} to remove from the game
     */
    public void removePlayer(final @NotNull CTFPlayer ctfPlayer) {
        final Team currentTeam = getTeam(ctfPlayer);

        if (currentTeam != null) {
            currentTeam.members().remove(ctfPlayer.uuid());
        }

        ctfPlayer.teamColor(null);
    }
}
