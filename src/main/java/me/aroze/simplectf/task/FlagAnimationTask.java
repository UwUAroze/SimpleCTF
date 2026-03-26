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
        this.isGoingUp = !this.isGoingUp;

        if (!this.display.isValid()) {
            this.cancel();
            return;
        }

        double deltaY = Y_OFFSET * (this.isGoingUp ? 1 : -1);

        this.display.setTeleportDuration(ROTATION_TICKS);

        this.location.setYaw(this.location.getYaw() + 120);
        this.location.setY(this.location.getY() + deltaY);

        this.display.teleport(this.location);
    }

}
