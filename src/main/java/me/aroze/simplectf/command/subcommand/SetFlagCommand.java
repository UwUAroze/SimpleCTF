package me.aroze.simplectf.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.command.argument.TeamArgumentType;
import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.team.FlagRetrievalType;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.team.TeamColor;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Accessors(fluent = true)
public final class SetFlagCommand {

    @Getter
    private static final SetFlagCommand instance = new SetFlagCommand();

    private static final Component ERROR_NO_TEAM = CtfMiniMessage.getInstance().deserialize("<warning>You must specify a team to set the flag of!");
    private static final Component ERROR_GAME_IN_PROGRESS = CtfMiniMessage.getInstance().deserialize("<warning>You can't set flag locations whilst a game is in progress!");

    private SetFlagCommand() {
    }

    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("setflag")
            .requires(source -> source.getSender() instanceof Player && source.getSender().hasPermission("simplectf.setflag"))
            .executes(this::executeNoTeam)
            .then(Commands.argument("team", TeamArgumentType.instance())
                .executes(this::execute)
            );
    }

    private int executeNoTeam(final CommandContext<CommandSourceStack> command) {
        command.getSource().getSender().sendMessage(ERROR_NO_TEAM);
        return Command.SINGLE_SUCCESS;
    }

    private int execute(final CommandContext<CommandSourceStack> command) {
        final Player player = (Player) command.getSource().getSender();

        if (CTFGame.instance().gameState() != GameState.WAITING) {
            player.sendMessage(ERROR_GAME_IN_PROGRESS);
            return Command.SINGLE_SUCCESS;
        }

        final TeamColor teamColor = command.getArgument("team", TeamColor.class);
        final Team team = CTFGame.instance().getTeam(teamColor);

        final Location location = player.getLocation().clone();
        location.setX(location.getBlockX() + 0.5);
        location.setZ(location.getBlockZ() + 0.5);

        team.baseLocation(location);
        team.retrieveFlag(FlagRetrievalType.RESET, null);

        player.sendMessage(CtfMiniMessage.getInstance().deserialize(
            "<s><tick> <p>Set flag location for <team>",
            Placeholder.unparsed("tick", Unicode.TICK),
            Placeholder.component("team", teamColor.formattedDisplayName())
        ));

        return Command.SINGLE_SUCCESS;
    }

}
