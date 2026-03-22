package me.aroze.simplectf.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.command.subcommand.JoinTeamCommand;

@Accessors(fluent = true)
public final class CTFCommand {

    @Getter
    private static final CTFCommand instance = new CTFCommand();

    private CTFCommand() {
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("ctf")
            .then(JoinTeamCommand.instance().create())
            .build();
    }

}

