package me.aroze.simplectf.game;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.task.RespawnTask;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.team.TeamColor;
import me.aroze.simplectf.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents an ongoing Capture The Flag game, providing methods for retrieving and modifying game state.
 */
@Accessors(fluent = true)
public final class CTFGame {

    /** Singleton instance of the CTFGame */
    @Getter
    private static final CTFGame instance = new CTFGame();

    @Getter
    private GameState gameState = GameState.WAITING;

    private final Map<TeamColor, Team> teams = new EnumMap<>(TeamColor.class);

    private CTFGame() {
        for (final TeamColor teamColor : TeamColor.values()) {
            teams.put(teamColor, new Team(teamColor));
        }
    }

    /**
     * Starts (or restarts) the game, teleporting all queued players to their base locations & populating inventories.
     */
    public void start() {
        for (final Team team : getAllTeams()) {
            final @Nullable Location baseLocation = team.baseLocation();
            team.retrieveFlag(null);

            for (final CTFPlayer ctfPlayer : team.ctfPlayers()) {
                RespawnTask.respawnPlayer(ctfPlayer.bukkitPlayer(), ctfPlayer);
            }
        }

        gameState = GameState.IN_PROGRESS;
    }

    /**
     * Stops the game, resetting all game state and clearing inventories
     */
    public void stop() {
        for (final Team team : getAllTeams()) {
            final @Nullable Location baseLocation = team.baseLocation();
            if (baseLocation == null) {
                continue;
            }

            for (final CTFPlayer ctfPlayer : team.ctfPlayers()) {
                PlayerUtil.reset(ctfPlayer.bukkitPlayer());
            }

            team.retrieveFlag(null);
        }
        gameState = GameState.WAITING;
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

    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    public Collection<UUID> getAllPlayers() {
        Collection<UUID> players = new ArrayList<>(List.of());
        for (Team team : getAllTeams()) {
            players.addAll(team.members());
        }
        return players;
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
            removePlayer(ctfPlayer);
        }

        final Team newTeam = teams.get(teamColor);
        newTeam.members().add(ctfPlayer.uuid());

        ctfPlayer.teamColor(teamColor);

        final Player bukkitPlayer = ctfPlayer.bukkitPlayer();
        final Component coloredName = Component.text(bukkitPlayer.getName(), teamColor.color());
        bukkitPlayer.displayName(coloredName);
        bukkitPlayer.playerListName(coloredName);

        if (gameState == GameState.IN_PROGRESS) {
            RespawnTask.respawnPlayer(bukkitPlayer, ctfPlayer);
        }
    }

    /**
     * Safely removes the given {@link CTFPlayer} from the game, resetting or removing all state associated with them.
     *
     * @param ctfPlayer the {@link CTFPlayer} to remove from the game
     */
    public void removePlayer(final @NotNull CTFPlayer ctfPlayer) {
        final Team currentTeam = getTeam(ctfPlayer);

        if (currentTeam != null) {
            currentTeam.members().remove(ctfPlayer.uuid());
        }

        if (ctfPlayer.carryingFlag() != null) {
            final Team flagTeam = teams.get(ctfPlayer.carryingFlag());
            flagTeam.dropFlag(ctfPlayer.bukkitPlayer().getLocation());
        }

        ctfPlayer.teamColor(null);

        final Player bukkitPlayer = ctfPlayer.bukkitPlayer();
        bukkitPlayer.displayName(null);
        bukkitPlayer.playerListName(null);
    }
}
