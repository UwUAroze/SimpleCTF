package me.aroze.simplectf.game;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.SimpleCTF;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.task.GameTickTask;
import me.aroze.simplectf.task.RespawnTask;
import me.aroze.simplectf.team.FlagRetrievalType;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.team.TeamColor;
import me.aroze.simplectf.util.PlayerUtil;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

/**
 * Represents an ongoing Capture The Flag game, providing methods for retrieving and modifying game state.
 */
@Accessors(fluent = true)
public final class CTFGame {

    /**
     * Singleton instance of the CTFGame
     */
    @Getter
    private static final CTFGame instance = new CTFGame();

    /**
     * Number of points needed to win
     */
    public static final int WINNING_SCORE = 3;

    /**
     * The current {@link GameState}
     */
    @Getter
    private GameState gameState = GameState.WAITING;

    /**
     * The bossbar shown to all players in the game
     */
    @Getter
    private final BossBar bossBar;

    /**
     * The task responsible for ticking the game timer
     */
    @Getter
    private @Nullable GameTickTask gameTickTask = null;

    private final Map<TeamColor, Team> teams = new EnumMap<>(TeamColor.class);

    private CTFGame() {
        for (final TeamColor teamColor : TeamColor.values()) {
            teams.put(teamColor, new Team(teamColor));
        }

        this.bossBar = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
    }

    /**
     * Starts (or restarts) the game, teleporting all queued players to their base locations & populating inventories.
     */
    public void start() {
        for (final Team team : getAllTeams()) {
            team.retrieveFlag(FlagRetrievalType.RESET, null);

            for (final CTFPlayer ctfPlayer : team.ctfPlayers()) {
                RespawnTask.respawnPlayer(ctfPlayer.bukkitPlayer(), ctfPlayer);
            }
        }

        gameState = GameState.IN_PROGRESS;
        startTickTask();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }
    }

    /**
     * Stops the game, declares winners & resets all game state along with clearing inventories
     */
    public void stop() {
        cancelTickTask();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bossBar);
        }

        final Map<Integer, List<TeamColor>> teamScores = new HashMap<>();
        int highestScore = 0;

        for (final Team team : getAllTeams()) {
            final int score = team.score();
            teamScores.computeIfAbsent(score, ArrayList::new).add(team.color());
            if (score > highestScore) {
                highestScore = score;
            }

            final @Nullable Location baseLocation = team.baseLocation();
            if (baseLocation == null) {
                continue;
            }

            team.reset();
        }

        gameState = GameState.WAITING;

        final List<TeamColor> winningTeams = teamScores.get(highestScore);
        if (winningTeams.isEmpty()) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f));
        displayWinners(winningTeams);
    }

    /**
     * Retrieves {@link Team} info by {@link TeamColor}.
     *
     * @param teamColor the {@link TeamColor} for which to retrieve the team
     * @return the associated {@link Team} instance
     */
    @NotNull
    public Team getTeam(final @NotNull TeamColor teamColor) {
        return teams.get(teamColor);
    }

    /**
     * Retrieves a collection of all teams currently in the game.
     *
     * @return collection of all teams in the game
     */
    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    /**
     * Retrieves a collection of all players currently in the game, across all teams.
     *
     * @return collection of all player UUIDs in the game
     */
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
        CTFScoreboard.instance().getCTFTeam(teamColor).addPlayer(bukkitPlayer);

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
        final Player bukkitPlayer = ctfPlayer.bukkitPlayer();

        if (currentTeam != null) {
            currentTeam.members().remove(ctfPlayer.uuid());
            CTFScoreboard.instance().getCTFTeam(currentTeam.color()).removePlayer(bukkitPlayer);
        }

        if (ctfPlayer.carryingFlag() != null) {
            final Team flagTeam = teams.get(ctfPlayer.carryingFlag());
            flagTeam.dropFlag(ctfPlayer.bukkitPlayer().getLocation(), true);
        }

        ctfPlayer.teamColor(null);

        bukkitPlayer.displayName(null);
        bukkitPlayer.playerListName(null);
        PlayerUtil.reset(bukkitPlayer);
    }

    private void displayWinners(final @NotNull List<TeamColor> winningTeams) {
        if (winningTeams.isEmpty()) {
            return;
        }

        final Component titleLine;
        final Component winnersLine;

        if (winningTeams.size() == 1) {
            final TeamColor winningTeam = winningTeams.getFirst();
            titleLine = Component.text("Game Over!", CtfMiniMessage.TERTIARY_COLOR);
            winnersLine = Component.text(Unicode.CROWN + " " + winningTeam.displayName() + " Team", winningTeam.color());
        } else {
            titleLine = Component.text("It's a draw!", CtfMiniMessage.TERTIARY_COLOR);
            winnersLine = Component.join(JoinConfiguration.spaces(),
                winningTeams.stream().map(team -> Component.text(Unicode.CROWN + " " + team.displayName() + " Team", team.color())).toList()
            );
        }

        final Title winnersTitle = Title.title(titleLine, winnersLine, Title.Times.times(
            Duration.ZERO,
            Duration.ofSeconds(5),
            Duration.ofSeconds(1)
        ));

        final Component winnersMessage = Component.join(JoinConfiguration.newlines(),
            Component.empty(),
            titleLine,
            winnersLine,
            Component.empty()
        );

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.showTitle(winnersTitle);
            player.sendMessage(winnersMessage);
        });
    }

    private void startTickTask() {
        cancelTickTask();

        gameTickTask = new GameTickTask();
        gameTickTask.runTaskTimer(SimpleCTF.getInstance(), 0L, 20L);
    }

    private void cancelTickTask() {
        if (gameTickTask == null) {
            return;
        }

        gameTickTask.cancel();
        gameTickTask = null;
    }
}
