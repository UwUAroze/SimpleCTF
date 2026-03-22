package me.aroze.simplectf.player;

import lombok.Getter;
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
     * @param player the {@link Player} for which to retrieve the associated {@link CTFPlayer}
     * @return the {@link CTFPlayer} instance associated with the given {@link Player}
     */
    public CTFPlayer getPlayer(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), CTFPlayer::new);
    }

    /**
     * Removes tracking of any {@link CTFPlayer} associated with the given UUID.
     *
     * @param uuid the UUID of the player to stop tracking
     * @return the {@link CTFPlayer} that was removed, or {@code null} if no player was found
     */
    @Nullable
    public CTFPlayer removePlayer(UUID uuid) {
        return players.remove(uuid);
    }

}
