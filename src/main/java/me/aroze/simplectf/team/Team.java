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

    /** The {@link TeamColor} of this team */
    private final TeamColor color;

    /** The UUIDs of all members in this team */
    private final List<UUID> members = new ArrayList<>();

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
     * Drops the flag in the world at the given location, removing any previously dropped flag.
     *
     * @param location the location to drop the flag
     */
    public void dropFlag(@NotNull final Location location) {
        destroyFlag(null);
        droppedFlagLocation = location;
        validateEntities();
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

    public void validateEntities() {
        validateFlagDisplay();
        validateFlagInteraction();
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
        location.setYaw(0);
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
        }
    }

}

