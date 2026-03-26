package me.aroze.simplectf.game;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.SimpleCTF;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.task.GameTickTask;
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
import org.bukkit.GameMode;
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

    /** Singleton instance of the CTFGame */
    @Getter
    private static final CTFGame instance = new CTFGame();

    /** Number of points needed to win */
    public static final int WINNING_SCORE = 3;

    /** The current {@link GameState} */
    @Getter
    private GameState gameState = GameState.WAITING;

    /** The bossbar shown to all players in the game */
    @Getter
    private final BossBar bossBar;

    /** The task responsible for ticking the game timer */
    @Getter
    private @Nullable GameTickTask gameTickTask = null;

    private final Map<TeamColor, Team> teams = new EnumMap<>(TeamColor.class);

    private CTFGame() {
        for (final TeamColor teamColor : TeamColor.values()) {
            this.teams.put(teamColor, new Team(teamColor, this));
        }

        this.bossBar = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
    }

    /**
     * Starts (or restarts) the game, teleporting all queued players to their base locations & populating inventories.
     */
    public void start() {
        for (final Team team : this.getAllTeams()) {
            team.retrieveFlag(FlagRetrievalType.RESET, null);

            for (final CTFPlayer ctfPlayer : team.ctfPlayers()) {
                this.respawnPlayer(ctfPlayer.bukkitPlayer(), ctfPlayer);
            }
        }

        this.gameState = GameState.IN_PROGRESS;
        this.startTickTask();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(this.bossBar);
        }
    }

    /**
     * Stops the game, declares winners & resets all game state along with clearing inventories
     */
    public void stop() {
        this.cancelTickTask();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(this.bossBar);
        }

        final Map<Integer, List<TeamColor>> teamScores = new HashMap<>();
        int highestScore = 0;

        for (final Team team : this.getAllTeams()) {
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

        this.gameState = GameState.WAITING;

        final List<TeamColor> winningTeams = teamScores.get(highestScore);
        if (winningTeams.isEmpty()) {
            return;
        }

        for (final UUID uuid : this.getAllPlayers()) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }

        this.displayWinners(winningTeams);
    }

    /**
     * Retrieves {@link Team} info by {@link TeamColor}.
     *
     * @param teamColor the {@link TeamColor} for which to retrieve the team
     * @return the associated {@link Team} instance
     */
    @NotNull
    public Team getTeam(final @NotNull TeamColor teamColor) {
        return this.teams.get(teamColor);
    }

    /**
     * Retrieves a collection of all teams currently in the game.
     *
     * @return collection of all teams in the game
     */
    public Collection<Team> getAllTeams() {
        return this.teams.values();
    }

    /**
     * Retrieves a collection of all players currently in the game, across all teams.
     *
     * @return collection of all player UUIDs in the game
     */
    public Collection<UUID> getAllPlayers() {
        Collection<UUID> players = new ArrayList<>();
        for (Team team : this.getAllTeams()) {
            players.addAll(team.members());
        }
        return players;
    }

    /**
     * Retrieves {@link Team} info by checking what team {@link CTFPlayer} belongs to, may be {@code null} if the player
     * is not in a team
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

        return this.teams.get(color);
    }

    /**
     * Sets the team for a {@link CTFPlayer}. If the player is already on a team, they will be removed from that team
     * before being added to the new one.
     *
     * @param ctfPlayer the {@link CTFPlayer} for which to set the team
     * @param teamColor the {@link TeamColor} to set the player to
     */
    public void setTeam(final @NotNull CTFPlayer ctfPlayer, final @NotNull TeamColor teamColor) {
        final @Nullable Team currentTeam = this.getTeam(ctfPlayer);
        if (currentTeam != null) {
            this.removePlayer(ctfPlayer);
        }

        final @Nullable Team newTeam = this.teams.get(teamColor);
        if (newTeam == null) {
            return;
        }

        newTeam.members().add(ctfPlayer.uuid());

        ctfPlayer.teamColor(teamColor);

        final Player bukkitPlayer = ctfPlayer.bukkitPlayer();
        final Component coloredName = Component.text(bukkitPlayer.getName(), teamColor.color());
        bukkitPlayer.displayName(coloredName);
        bukkitPlayer.playerListName(coloredName);

        CTFScoreboard.instance().getCTFTeam(teamColor).addPlayer(bukkitPlayer);

        if (this.gameState == GameState.IN_PROGRESS) {
            this.respawnPlayer(bukkitPlayer, ctfPlayer);
        }
    }

    /**
     * Safely removes the given {@link CTFPlayer} from the game, resetting or removing all state associated with them.
     *
     * @param ctfPlayer the {@link CTFPlayer} to remove from the game
     */
    public void removePlayer(final @NotNull CTFPlayer ctfPlayer) {
        final @Nullable Team currentTeam = this.getTeam(ctfPlayer);
        final @Nullable Player bukkitPlayer = ctfPlayer.bukkitPlayer();
        if (bukkitPlayer == null) {
            return;
        }

        if (currentTeam != null) {
            currentTeam.members().remove(ctfPlayer.uuid());
            CTFScoreboard.instance().getCTFTeam(currentTeam.color()).removePlayer(bukkitPlayer);
        }

        if (ctfPlayer.carryingFlag() != null) {
            final Team flagTeam = this.teams.get(ctfPlayer.carryingFlag());
            flagTeam.dropFlag(bukkitPlayer.getLocation(), true);
        }

        ctfPlayer.teamColor(null);

        bukkitPlayer.displayName(null);
        bukkitPlayer.playerListName(null);
        PlayerUtil.reset(bukkitPlayer);
    }

    /**
     * Respawns the player, teleporting them to their spawn, resetting their health & other attributes along with
     * applying their team kit if they are in the game.
     *
     * @param player the {@link Player} to respawn
     * @param ctfPlayer the {@link CTFPlayer} representation of the player to respawn
     */
    public void respawnPlayer(final Player player, final CTFPlayer ctfPlayer) {
        PlayerUtil.reset(player);
        player.clearTitle();
        player.teleport(this.getRespawnLocation(player, ctfPlayer));
        player.setGameMode(GameMode.SURVIVAL);

        final @Nullable TeamColor teamColor = ctfPlayer.teamColor();
        if (teamColor != null) {
            teamColor.kit().applyKit(player);
        }
    }

    private Location getRespawnLocation(final Player player, final CTFPlayer ctfPlayer) {
        final @Nullable TeamColor teamColor = ctfPlayer.teamColor();
        if (teamColor == null) {
            return player.getWorld().getSpawnLocation(); // Fallback, e.g. player left the game while respawning
        }

        final Team team = this.getTeam(teamColor);
        return team.baseLocation() == null
            ? player.getWorld().getSpawnLocation() // Shouldn't happen!
            : team.baseLocation();
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

        for (final UUID uuid : this.getAllPlayers()) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            player.showTitle(winnersTitle);
            player.sendMessage(winnersMessage);
        };
    }

    private void startTickTask() {
        this.cancelTickTask();

        this.gameTickTask = new GameTickTask(this);
        this.gameTickTask.runTaskTimer(SimpleCTF.instance(), 0L, 20L);
    }

    private void cancelTickTask() {
        if (this.gameTickTask == null) {
            return;
        }

        this.gameTickTask.cancel();
        this.gameTickTask = null;
    }
}
