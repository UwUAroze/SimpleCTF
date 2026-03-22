package me.aroze.simplectf;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.aroze.simplectf.command.CTFCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SimpleCTF extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
           commands.registrar().register(CTFCommand.instance().build(), List.of("capturetheflag"));
        });

        System.out.println("Enabled SimpleCTF");
    }

    @Override
    public void onDisable() {
        System.out.println("Disabled");
    }

}
