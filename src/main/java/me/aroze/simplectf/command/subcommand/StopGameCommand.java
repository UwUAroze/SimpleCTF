package me.aroze.simplectf.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

@Accessors(fluent = true)
public final class StopGameCommand {

    @Getter
    private static final StopGameCommand instance = new StopGameCommand();

    private static final Component ERROR_GAME_NOT_IN_PROGRESS = CtfMiniMessage.getInstance().deserialize("<warning>The game isn't in progress, are you looking for /ctf start?");

    private StopGameCommand() {
    }

    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("stop")
            .requires(source -> source.getSender().hasPermission("simplectf.stop"))
            .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        final CommandSender sender = command.getSource().getSender();

        if (CTFGame.instance().gameState() != GameState.IN_PROGRESS) {
            sender.sendMessage(ERROR_GAME_NOT_IN_PROGRESS);
            return Command.SINGLE_SUCCESS;
        }

        CTFGame.instance().stop();
        return Command.SINGLE_SUCCESS;
    }

}
