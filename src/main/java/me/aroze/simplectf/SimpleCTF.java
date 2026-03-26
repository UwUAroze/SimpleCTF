package me.aroze.simplectf;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.command.CTFCommand;
import me.aroze.simplectf.game.CTFScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.ServiceLoader;

@Getter
@Accessors(fluent = true)
public final class SimpleCTF extends JavaPlugin {

    @Override
    public void onEnable() {
        CTFScoreboard.instance().unregisterTeams();

        this.registerCommands();
        this.registerListeners();
    }

    @Override
    public void onDisable() {
        CTFScoreboard.instance().unregisterTeams();
    }

    private void registerListeners() {
        ServiceLoader.load(Listener.class, this.getClassLoader()).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(CTFCommand.instance().build(), List.of("simplectf", "capturetheflag"));
        });
    }

    /**
     * Retrieves the singleton instance of the SimpleCTF plugin.
     *
     * @return {@link SimpleCTF} plugin instance
     */
    public static SimpleCTF instance() {
        return JavaPlugin.getPlugin(SimpleCTF.class);
    }

}
