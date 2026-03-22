package me.aroze.simplectf;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.command.CTFCommand;
import me.aroze.simplectf.listener.ConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class SimpleCTF extends JavaPlugin {

    @Override
    public void onEnable() {
        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        // ...
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(CTFCommand.instance().build(), List.of("simplectf", "capturetheflag"));
        });
    }

    /**
     * Retrieves the singleton instance of the SimpleCTF plugin.
     * @return {@link SimpleCTF} plugin instance
     */
    public static SimpleCTF getInstance() {
        return JavaPlugin.getPlugin(SimpleCTF.class);
    }

}
