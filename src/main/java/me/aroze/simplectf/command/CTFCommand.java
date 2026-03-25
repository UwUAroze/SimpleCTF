package me.aroze.simplectf.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.command.subcommand.*;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;

@Accessors(fluent = true)
public final class CTFCommand {

    @Getter
    private static final CTFCommand instance = new CTFCommand();

    private CTFCommand() {
    }

    private static final Component INFO = CtfMiniMessage.getInstance().deserialize("<s>SimpleCTF commands")
        .append(createUsageLine("join <team>", "join or switch teams"))
        .append(createUsageLine("leave", "leave the current game"))
        .append(createUsageLine("score", "show live team scores"))
        .append(createUsageLine("setflag <team>", "set a team's flag/base location"))
        .append(createUsageLine("start", "start a game"))
        .append(createUsageLine("stop", "stop the current game"));

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("ctf")
            .executes(this::executeInfo)
            .then(JoinCommand.instance().create())
            .then(LeaveCommand.instance().create())
            .then(ScoreCommand.instance().create())
            .then(SetFlagCommand.instance().create())
            .then(StartGameCommand.instance().create())
            .then(StopGameCommand.instance().create())
            .build();
    }

    private int executeInfo(final CommandContext<CommandSourceStack> command) {
        final CommandSender sender = command.getSource().getSender();
        sender.sendMessage(INFO);

        return Command.SINGLE_SUCCESS;
    }

    private static Component createUsageLine(final String subcommand, final String description) {
        return CtfMiniMessage.getInstance().deserialize("\n <s>• <p>/ctf " + subcommand + " <s>- " + description)
            .hoverEvent(CtfMiniMessage.getInstance().deserialize("<p>Click to type <s>/ctf " + subcommand))
            .clickEvent(ClickEvent.suggestCommand("/ctf " + subcommand));
    }

}

