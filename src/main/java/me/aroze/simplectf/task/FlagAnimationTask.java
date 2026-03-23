package me.aroze.simplectf.task;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.scheduler.BukkitRunnable;

@Accessors(fluent = true)
public final class FlagAnimationTask extends BukkitRunnable {

    @Getter
    private static final int ROTATION_TICKS = 24;
    private static final double Y_OFFSET = 0.3;

    private final Display display;
    private final Location location;

    private boolean isGoingUp = false;

    public FlagAnimationTask(final Display display) {
        this.display = display;
        this.location = display.getLocation().clone();
    }

    @Override
    public void run() {
        isGoingUp = !isGoingUp;

        if (!display.isValid()) {
            this.cancel();
            return;
        }

        double deltaY = Y_OFFSET * (isGoingUp ? 1 : -1);

        display.setTeleportDuration(ROTATION_TICKS);

        location.setYaw(location.getYaw() + 120);
        location.setY(location.getY() + deltaY);

        display.teleport(location);
    }

}
