package me.aroze.simplectf.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.game.GameState;
import me.aroze.simplectf.team.Team;
import me.aroze.simplectf.team.TeamColor;
import me.aroze.simplectf.util.text.ComponentUtil;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

@Accessors(fluent = true)
public final class StartGameCommand {

    @Getter
    private static final StartGameCommand instance = new StartGameCommand();

    private static final Component ERROR_NOT_ENOUGH_PLAYERS = CtfMiniMessage.getInstance().deserialize("<warning>There must be at least 2 players in the game to start!");
    private static final Component ERROR_GAME_IN_PROGRESS = CtfMiniMessage.getInstance().deserialize("<warning>The game is in progress, are you looking for /ctf stop?");
    private static final Component SUCCESS_GAME_STARTED = CtfMiniMessage.getInstance().deserialize("<s><tick> <p>You've started the game, enjoy!",
        Placeholder.unparsed("tick", Unicode.TICK)
    );

    private StartGameCommand() {
    }

    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("start")
            .requires(source -> source.getSender().hasPermission("simplectf.start"))
            .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        final CommandSender sender = command.getSource().getSender();

        final Set<TeamColor> missingBaseLocations = new HashSet<>();
        for (final Team team : CTFGame.instance().getAllTeams()) {
            if (team.baseLocation() == null) {
                missingBaseLocations.add(team.color());
            }
        }

        if (!missingBaseLocations.isEmpty()) {
            sender.sendMessage(CtfMiniMessage.getInstance().deserialize(
                "<warning>Missing flag locations for <teams>",
                Placeholder.component("teams", Component.join(ComponentUtil.JOIN_CONFIGURATION_COMMA_AND(),
                    missingBaseLocations.stream().map(TeamColor::formattedDisplayName).toList()
                )))
            );
            return Command.SINGLE_SUCCESS;
        }

        if (CTFGame.instance().getAllPlayers().size() < 2) {
            sender.sendMessage(ERROR_NOT_ENOUGH_PLAYERS);
            return Command.SINGLE_SUCCESS;
        }

        if (CTFGame.instance().gameState() != GameState.WAITING) {
            sender.sendMessage(ERROR_GAME_IN_PROGRESS);
            return Command.SINGLE_SUCCESS;
        }

        CTFGame.instance().start();
        sender.sendMessage(SUCCESS_GAME_STARTED);
        return Command.SINGLE_SUCCESS;
    }

}
