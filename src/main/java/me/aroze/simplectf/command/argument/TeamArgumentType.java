package me.aroze.simplectf.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.aroze.simplectf.team.TeamColor;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
public final class TeamArgumentType implements CustomArgumentType.Converted<TeamColor, String> {

    @Getter
    private static final TeamArgumentType instance = new TeamArgumentType();

    private static final DynamicCommandExceptionType ERROR_INVALID_TEAM = new DynamicCommandExceptionType(teamColor ->
        MessageComponentSerializer.message().serialize(CtfMiniMessage.getInstance().deserialize("<warning>'" + teamColor + "' is not a valid team!"))
    );

    @Override
    public @NonNull TeamColor convert(final String nativeType) throws CommandSyntaxException {
        try {
            return TeamColor.valueOf(nativeType.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            throw ERROR_INVALID_TEAM.create(nativeType);
        }
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(final @NotNull CommandContext<S> context, final @NotNull SuggestionsBuilder builder) {
        for (TeamColor teamColor : TeamColor.values()) {
            String name = teamColor.name().toLowerCase();
            if (name.startsWith(builder.getRemaining())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

}

