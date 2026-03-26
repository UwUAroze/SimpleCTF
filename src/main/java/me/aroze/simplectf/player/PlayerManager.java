package me.aroze.simplectf.player;

import lombok.Getter;
import me.aroze.simplectf.team.TeamColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the lifecycle of {@link CTFPlayer}s
 */
public final class PlayerManager {

    /** Singleton instance of the PlayerManager */
    @Getter
    private static final PlayerManager instance = new PlayerManager();

    private PlayerManager() {
    }

    private final Map<UUID, CTFPlayer> players = new HashMap<>();

    /**
     * Retrieves the {@link CTFPlayer} associated with the given {@link Player}, creating and keeping track of a new
     * instance if needed.
     *
     * @param player the Bukkit {@link Player}
     * @return the player's {@link CTFPlayer} instance
     */
    public CTFPlayer getPlayer(final Player player) {
        return this.getPlayer(player.getUniqueId());
    }

    /**
     * Retrieves the {@link CTFPlayer} associated with the given player {@link UUID}, creating and keeping track of a new
     * instance if needed.
     *
     * @param uuid the {@link UUID} of the player
     * @return the player's {@link CTFPlayer} instance
     */
    public CTFPlayer getPlayer(final UUID uuid) {
        return this.players.computeIfAbsent(uuid, CTFPlayer::new);
    }

    public @Nullable CTFPlayer findPlayerByCarryingFlag(final TeamColor queryTeam) {
        return this.players.values().stream()
            .filter(ctfPlayer -> queryTeam.equals(ctfPlayer.carryingFlag()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Removes tracking of any {@link CTFPlayer} associated with the given UUID.
     *
     * @param uuid the UUID of the player to stop tracking
     * @return the {@link CTFPlayer} that was removed, or {@code null} if no player was found
     */
    @Nullable
    public CTFPlayer removePlayer(UUID uuid) {
        return this.players.remove(uuid);
    }

}
