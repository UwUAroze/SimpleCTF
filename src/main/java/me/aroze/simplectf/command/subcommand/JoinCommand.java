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
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true)
public final class JoinCommand {

    @Getter
    private static final JoinCommand instance = new JoinCommand();

    private static final Component ERROR_NO_TEAM = CtfMiniMessage.getInstance().deserialize("<warning>You must specify a team!");

    private JoinCommand() {
    }

    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("join")
            .requires(source -> source.getSender() instanceof Player)
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
        final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(player);

        final @Nullable TeamColor currentTeam = ctfPlayer.teamColor();
        final TeamColor newTeam = command.getArgument("team", TeamColor.class);

        if (currentTeam == newTeam) {
            player.sendMessage(CtfMiniMessage.getInstance().deserialize(
                "<warning>You're already on <team>",
                Placeholder.component("team", newTeam.formattedDisplayName())
            ));
            return Command.SINGLE_SUCCESS;
        }

        final String action = currentTeam == null
            ? "joined"
            : "switched to";

        CTFGame.instance().setTeam(ctfPlayer, newTeam);

        player.sendMessage(CtfMiniMessage.getInstance().deserialize(
            "<s><arrow> <p>You've <action> <team>",
            Placeholder.unparsed("arrow", Unicode.RIGHT_ARROW),
            Placeholder.unparsed("action", action),
            Placeholder.component("team", newTeam.formattedDisplayName())
        ));

        return Command.SINGLE_SUCCESS;
    }

}
