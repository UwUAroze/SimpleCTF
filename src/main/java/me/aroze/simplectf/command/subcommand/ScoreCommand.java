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
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

@Accessors(fluent = true)
public final class ScoreCommand {

    @Getter
    private static final ScoreCommand instance = new ScoreCommand();

    private static final Component ERROR_GAME_NOT_IN_PROGRESS = CtfMiniMessage.getInstance().deserialize("<warning>The game isn't in progress!");

    private ScoreCommand() {
    }

    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("score")
            .executes(this::execute);
    }

    private int execute(final CommandContext<CommandSourceStack> command) {
        final CommandSender sender = command.getSource().getSender();

        if (CTFGame.instance().gameState() != GameState.IN_PROGRESS) {
            sender.sendMessage(ERROR_GAME_NOT_IN_PROGRESS);
            return Command.SINGLE_SUCCESS;
        }

        Component scoreMessage = CtfMiniMessage.getInstance().deserialize("<p>Current Scores:</p>");
        for (final Team team : CTFGame.instance().getAllTeams()) {
            scoreMessage = scoreMessage.appendNewline().append(buildTeamScoreLine(team));
        }

        sender.sendMessage(scoreMessage);
        return Command.SINGLE_SUCCESS;
    }

    private Component buildTeamScoreLine(final Team team) {
        Component line = Component.text(Unicode.FLAG + " " + team.color().displayName() + " ", team.color().color());

        for (int i = 0; i < CTFGame.WINNING_SCORE; i++) {
            final TextColor circleColor = i < team.score()
                ? team.color().color()
                : NamedTextColor.GRAY;

            line = line.append(Component.text(Unicode.CIRCLE, circleColor));
        }

        return line;
    }
}


