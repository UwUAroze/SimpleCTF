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
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.team.TeamColor;
import org.bukkit.entity.Player;

@Accessors(fluent = true)
public final class JoinTeamCommand {

    @Getter
    private static final JoinTeamCommand instance = new JoinTeamCommand();

    private JoinTeamCommand() {
    }

    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("join")
            .then(Commands.argument("team", TeamArgumentType.instance())
                .requires(source -> source.getSender() instanceof Player)
                .executes(this::execute)
            );
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        final Player player = (Player) command.getSource().getSender();
        final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(player.getUniqueId());

        if (ctfPlayer.teamColor() != null) {
            // todo: prompt to leave game first
            return Command.SINGLE_SUCCESS;
        }

        final TeamColor teamColor = command.getArgument("team", TeamColor.class);
        CTFGame.instance().setTeam(ctfPlayer, teamColor);

        // todo: joined team
        return Command.SINGLE_SUCCESS;
    }

}
