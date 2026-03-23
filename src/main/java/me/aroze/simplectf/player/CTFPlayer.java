package me.aroze.simplectf.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player being tracked by the CTF minigame, storing data relevant to game state. All online players have
 * a corresponding {@link CTFPlayer} instance.
 *
 * @see PlayerManager for managing the lifecycle of these instances
 */
@Getter
@Setter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class CTFPlayer {

    /** The Minecraft UUID of this player */
    private final UUID uuid;

    /** The team color of this player, or null if the player is not on any team */
    private @Nullable TeamColor teamColor = null;

    /** The team color of the flag this player is currently carrying, or null if the player is not carrying any flag */
    private @Nullable TeamColor carryingFlag = null;

    /** Whether the player is currently in the respawn sequence */
    private boolean isRespawning = false;

    /**
     * Retrieves the associated Bukkit {@link Player}
     *
     * @return the Bukkit {@link Player} associated with this CTFPlayer
     */
    public Player bukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

}
