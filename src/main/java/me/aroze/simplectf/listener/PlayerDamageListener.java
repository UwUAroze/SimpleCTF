package me.aroze.simplectf.listener;

import com.google.auto.service.AutoService;
import me.aroze.simplectf.SimpleCTF;
import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.task.RespawnTask;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.team.TeamColor;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.Nullable;

@AutoService(Listener.class)
public final class PlayerDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof final Player victim)) return;

        if (CTFGame.instance().gameState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        @Nullable Entity entity = event.getDamager();
        @Nullable Integer projectileDistance = null;

        @Nullable Player damagerPlayer = switch (entity) {
            case Player player -> player;
            case Projectile projectile -> {
                if (projectile.getShooter() instanceof final Player shooter) {
                    projectileDistance = (int) Math.round(shooter.getLocation().distance(victim.getLocation()));
                    yield shooter;
                }
                yield null;
            }
            default -> null;
        };

        if (damagerPlayer != null) {
            final CTFPlayer ctfDamager = PlayerManager.getInstance().getPlayer(damagerPlayer);
            final CTFPlayer ctfVictim = PlayerManager.getInstance().getPlayer(victim);

            // Allow damage to self (bow boosting, etc.) but prevent damage to teammates and non players
            if (ctfDamager != ctfVictim && (ctfDamager.teamColor() == null || ctfVictim.teamColor() == null || ctfDamager.teamColor() == ctfVictim.teamColor())) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getFinalDamage() < victim.getHealth()) return; // Damage didn't result in kill.

        event.setDamage(0.0);
        this.postDeathTasks(victim, damagerPlayer, projectileDistance);
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        event.setCancelled(true);
        final Player player = event.getEntity();
        this.postDeathTasks(player, player.getKiller(), null);
    }

    private void postDeathTasks(final Player player, final @Nullable Player killer, final @Nullable Integer projectileDistance) {
        final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(player);

        Bukkit.broadcast(this.constructDeathBroadcast(player, killer, projectileDistance));

        final TeamColor flagColor = ctfPlayer.carryingFlag();
        if (flagColor != null) {
            final Team team = CTFGame.instance().getTeam(flagColor);
            team.dropFlag(player.getLocation(), true);
        }

        final RespawnTask task = new RespawnTask(CTFGame.instance(), player);
        task.start();
        task.runTaskTimer(SimpleCTF.instance(), 0L, 1L);

        if (killer != null) {
            killer.playSound(killer, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1f, 1f);
        }
    }

    private Component constructDeathBroadcast(final Player player, final @Nullable Player killer, final @Nullable Integer projectileDistance) {
        if (player == killer) {
            // This could happen by shooting yourself with a bow, etc
            return CtfMiniMessage.getInstance().deserialize("<#ff6378><skull> <#ffb3bf><victim> <#e3bac0>killed themselves",
                Placeholder.unparsed("skull", Unicode.SKULL),
                Placeholder.component("victim", player.displayName())
            );
        }

        if (killer == null) {
            return CtfMiniMessage.getInstance().deserialize("<#ff6378><skull> <#ffb3bf><victim> <#e3bac0>died to natural causes",
                Placeholder.unparsed("skull", Unicode.SKULL),
                Placeholder.component("victim", player.displayName())
            );
        }

        if (projectileDistance == null) {
            return CtfMiniMessage.getInstance().deserialize("<#ff6378><sword> <#ffb3bf><victim> <#e3bac0>was stabbed by <#ffb3bf><killer>",
                Placeholder.unparsed("sword", Unicode.SWORD),
                Placeholder.component("victim", player.displayName()),
                Placeholder.component("killer", killer.displayName())
            );
        }

        return CtfMiniMessage.getInstance().deserialize("<#ff6378><bow> <#ffb3bf><victim> <#e3bac0>was shot by <#ffb3bf><killer> <#e3bac0>(<distance>m)",
            Placeholder.unparsed("bow", Unicode.BOW),
            Placeholder.component("victim", player.displayName()),
            Placeholder.component("killer", killer.displayName()),
            Placeholder.unparsed("distance", projectileDistance.toString())
        );
    }

}
