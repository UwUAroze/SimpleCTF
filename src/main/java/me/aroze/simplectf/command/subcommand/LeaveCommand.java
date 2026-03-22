package me.aroze.simplectf.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.game.CTFGame;
import me.aroze.simplectf.player.CTFPlayer;
import me.aroze.simplectf.player.PlayerManager;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import me.aroze.simplectf.util.text.Unicode;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Accessors(fluent = true)
public final class LeaveCommand {

    @Getter
    private static final LeaveCommand instance = new LeaveCommand();

    private static final Component ERROR_NOT_IN_GAME = CtfMiniMessage.getInstance().deserialize("<warning>You're not in a game, use /ctf join <team> to join!");
    private static final Component SUCCESS_LEFT_GAME = CtfMiniMessage.getInstance().deserialize("<s>" + Unicode.LEFT_ARROW + " <p>You've left the game");

    private LeaveCommand() {
    }

    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("leave")
            .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        final Player player = (Player) command.getSource().getSender();
        final CTFPlayer ctfPlayer = PlayerManager.getInstance().getPlayer(player);

        if (ctfPlayer.teamColor() == null) {
            player.sendMessage(ERROR_NOT_IN_GAME);
            return Command.SINGLE_SUCCESS;
        }

        CTFGame.instance().removePlayer(ctfPlayer);

        player.sendMessage(SUCCESS_LEFT_GAME);
        return Command.SINGLE_SUCCESS;
    }

}
