package me.aroze.simplectf.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.SimpleCTF;
import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.task.FlagAnimationTask;
import me.aroze.simplectf.task.GameTickTask;
import me.aroze.simplectf.util.PlayerUtil;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Represents data for a team in a {@link CTFGame}
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class Team {

    /** The radius of the base, centered around {@link #baseLocation} */
    private static final int BASE_RADIUS = 3;

    /** {@link #BASE_RADIUS} squared, used for faster distance calculations */
    private static final int BASE_RADIUS_SQUARED = BASE_RADIUS * BASE_RADIUS;

    /** The {@link TeamColor} of this team */
    private final TeamColor color;

    /** The UUIDs of all members in this team */
    private final List<UUID> members = new ArrayList<>();

    /** The current score of the team */
    private int score = 0;

    /** The {@link Location} where the flag and team members will spawn. May be null if the base has not been set yet. */
    @Setter
    private @Nullable Location baseLocation = null;

    /** The {@link Location} where the flag is dropped, may be null if the flag is not dropped */
    private @Nullable Location droppedFlagLocation = null;

    /** The UUID of the {@link BlockDisplay} representing the team's flag, may be null if the flag is not dropped */
    private @Nullable UUID flagDisplayUUID = null;

    /** The UUID of the {@link org.bukkit.entity.Interaction} representing the interaction hitbox of the team's flag, may be null if the flag is not dropped */
    private @Nullable UUID flagInteractionUUID = null;

    /** The ID of the Bukkit task for the flag's spinning animation */
    private @Nullable Integer flagAnimationTaskId = null;

    /**
     * Retrieves the list of team members as a list of {@link CTFPlayer}
     *
     * @return all {@link CTFPlayer} in the team
     */
    public Collection<CTFPlayer> ctfPlayers() {
        return members.stream()
            .map(PlayerManager.getInstance()::getPlayer)
            .toList();
    }

    /**
     * Checks whether the flag is at the team's base
     *
     * @return {@code true} if the flag is at the team's base, or {@code false} if dropped elsewhere
     */
    public boolean isFlagAtBase() {
        return droppedFlagLocation == baseLocation;
    }

    /**
     * Increments the team's score by 1, checking for a win condition and instantly updates the score display if
     * necessary.
     */
    public void incrementScore() {
        score++;
        if (score >= CTFGame.WINNING_SCORE) {
            CTFGame.instance().stop();
            return;
        }

        final @Nullable GameTickTask gameTickTask = CTFGame.instance().gameTickTask();
        if (gameTickTask != null) {
            gameTickTask.tick();
        }
    }

    /**
     * Resets all game state related to this team (score back to 0, flag back at base and resetting state of team
     * members).
     */
    public void reset() {
        score = 0;
        retrieveFlag(FlagRetrievalType.RESET, null);

        for (final CTFPlayer ctfPlayer : this.ctfPlayers()) {
            PlayerUtil.reset(ctfPlayer.bukkitPlayer());
        }
    }

    /**
     * Checks whether the given location is in the team's base by checking the location's distance to the base and
     * comparing to {@link #BASE_RADIUS}. If the base location is not set, this method will always return {@code false}.
     *
     * @param location the {@link Location} to check
     * @return {@code true} if the Location is in the base, otherwise {@code false}
     */
    public boolean isInBase(final Location location) {
        if (baseLocation == null) return false;
        return location.distanceSquared(baseLocation) < BASE_RADIUS_SQUARED;
    }

    /**
     * Drops the flag in the world at the given location, removing any previously dropped flag & removing it from any
     * player carrying it
     *
     * @param location the location to drop the flag
     * @param broadcast whether to broadcast the flag being dropped by a player
     */
    public void dropFlag(@NotNull final Location location, final boolean broadcast) {
        @Nullable CTFPlayer flagHolder = PlayerManager.getInstance().findPlayerByCarryingFlag(color);
        if (flagHolder != null) {
            flagHolder.carryingFlag(null);
            flagHolder.bukkitPlayer().getInventory().setHelmet(null);
            if (broadcast) {
                Bukkit.broadcast(formatFlagBroadcast("<color><team>'s flag has been dropped by <player></color>", flagHolder.bukkitPlayer()));
            }
        }

        destroyFlag(null);
        droppedFlagLocation = location;
        validateEntities();
    }

    /**
     * Ensures entities relating to the team (flag display, interaction hitbox) are present in the world, respawning
     * them if necessary
     */
    public void validateEntities() {
        validateFlagDisplay();
        validateFlagInteraction();
    }

    /**
     * Safely retrieves the flag to the team, removing it if dropped and respawning it at the team's base.
     *
     * @param retriever the {@link Player} who retrieved the flag, or {@code null} if the flag is being reset without a
     * retriever.
     */
    public void retrieveFlag(final FlagRetrievalType retrievalType, final @Nullable Player retriever) {
        if (baseLocation == null) {
            return;
        }

        dropFlag(baseLocation, false);

        if (retriever == null) {
            return;
        }

        if (retrievalType == FlagRetrievalType.RETURNED) {
            retriever.playSound(retriever, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.5f);
            Bukkit.broadcast(formatFlagBroadcast("<color><team>'s flag has been returned by <player></color>", retriever));
            return;
        }

        if (retrievalType == FlagRetrievalType.CAPTURED) {
            retriever.playSound(retriever, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.5f);
            Bukkit.broadcast(formatFlagBroadcast("<color><team>'s flag has been captured by <player></color>", retriever));
        }
    }

    /**
     * Destroys the flag for the team, safely removing the display/interaction entity & tracking captured flag state if
     * a capturer is provided.
     *
     * @param capturer The {@link Player} who captured the flag, or {@code null} if the flag is not being captured
     */
    public void destroyFlag(final @Nullable Player capturer) {
        final @Nullable BlockDisplay flagDisplay = getFlagDisplay();
        if (flagDisplay != null) {
            flagDisplay.remove();
            flagDisplayUUID = null;
        }

        final @Nullable Interaction flagInteraction = getFlagInteraction();
        if (flagInteraction != null) {
            flagInteraction.remove();
            flagInteractionUUID = null;
        }

        if (flagAnimationTaskId != null) {
            Bukkit.getScheduler().cancelTask(flagAnimationTaskId);
        }

        droppedFlagLocation = null;

        if (capturer != null) {
            final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(capturer.getUniqueId());
            ctfPlayer.carryingFlag(color);

            capturer.getInventory().setHelmet(color.kit().retrieveFlagItem());
            capturer.playSound(capturer, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 2f);
            Bukkit.broadcast(formatFlagBroadcast("<color><team>'s flag has been picked up by <player></color>", capturer));
        }
    }

    private Component formatFlagBroadcast(final String input, final Player player) {
        return CtfMiniMessage.getInstance().deserialize(input,
            Placeholder.styling("color", color.color()),
            Placeholder.component("team", color.formattedDisplayName()),
            Placeholder.component("player", player.displayName())
        );
    }

    private @Nullable BlockDisplay getFlagDisplay() {
        if (flagDisplayUUID == null) {
            return null;
        }

        final @Nullable Entity entity = Bukkit.getEntity(flagDisplayUUID);
        if (entity == null || !entity.isValid() || !(entity instanceof BlockDisplay)) {
            return null;
        }

        return (BlockDisplay) entity;
    }

    private @Nullable Interaction getFlagInteraction() {
        if (flagInteractionUUID == null) {
            return null;
        }

        final @Nullable Entity entity = Bukkit.getEntity(flagInteractionUUID);
        if (entity == null || !entity.isValid() || !(entity instanceof Interaction)) {
            return null;
        }

        return (Interaction) entity;
    }

    private void validateFlagDisplay() {
        if (droppedFlagLocation == null) {
            return;
        }

        @Nullable BlockDisplay display = getFlagDisplay();
        if (display != null) {
            return;
        }

        final Location location = droppedFlagLocation.clone();
        location.setPitch(0);
        location.setY(location.getY() + 0.3); // Shifting up since we scale the model down

        display = location.getWorld().spawn(location, BlockDisplay.class);

        final Transformation transformation = new Transformation(
            new Vector3f(-0.375f, 0f, -0.375f),
            new Quaternionf(),
            new Vector3f(0.75f),
            new Quaternionf()
        );

        display.teleport(location);
        display.setTransformation(transformation);
        display.setBlock(color.flagType().createBlockData());
        display.setGlowing(true);
        display.setGlowColorOverride(color.bukkitColor());
        display.setTeleportDuration(FlagAnimationTask.ROTATION_TICKS());
        display.setPersistent(false);
        flagDisplayUUID = display.getUniqueId();

        flagAnimationTaskId = new FlagAnimationTask(display)
            .runTaskTimer(SimpleCTF.getInstance(), 2, FlagAnimationTask.ROTATION_TICKS())
            .getTaskId();
    }

    private void validateFlagInteraction() {
        if (droppedFlagLocation == null) {
            return;
        }

        @Nullable Interaction interaction = getFlagInteraction();
        if (interaction != null) {
            return;
        }

        final Location location = droppedFlagLocation.clone();
        location.setY(location.getY() + 0.3); // Shifting up since we scale the model down

        interaction = location.getWorld().spawn(location, Interaction.class);
        interaction.setInteractionWidth(1f);
        interaction.setInteractionHeight(1.7f);
        interaction.setPersistent(false);

        flagInteractionUUID = interaction.getUniqueId();
    }

}

