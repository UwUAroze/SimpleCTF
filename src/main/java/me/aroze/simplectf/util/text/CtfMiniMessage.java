package me.aroze.simplectf.util.text;

import lombok.Getter;
import lombok.val;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.ArrayList;
import java.util.List;

/**
 * MiniMessage instance with custom formatting
 */
public final class CtfMiniMessage {

    public static final TextColor PRIMARY_COLOR = TextColor.color(0xF2E0FF);
    public static final TextColor SECONDARY_COLOR = TextColor.color(0xEBC7FF);
    public static final TextColor TERTIARY_COLOR = TextColor.color(0xFFA3D1);

    /** Singleton instance of the built {@link MiniMessage} instance */
    @Getter
    private static final MiniMessage instance = new CtfMiniMessage().build();

    private CtfMiniMessage() {
    }

    private final List<TagResolver> format = new ArrayList<>();

    /**
     * Builds a {@link MiniMessage} instance with custom formatting
     *
     * @return The {@link MiniMessage} instance
     */
    public MiniMessage build() {
        this.addPreProcessed("warning", "<#ff6e6e>⚠ <#ff7f6e>");

        this.addColorCode("p", PRIMARY_COLOR);
        this.addColorCode("s", SECONDARY_COLOR);
        this.addColorCode("t", SECONDARY_COLOR);

        this.format.add(StandardTags.defaults());

        val resolvers = TagResolver.resolver(this.format);

        return MiniMessage.builder()
            .tags(resolvers)
            .postProcessor(component -> {
                component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                return component;
            })
            .build();
    }

    private void addColorCode(final String code, final TextColor textColor) {
        this.format.add(TagResolver.resolver(code, Tag.styling(textColor)));
    }

    private void addPreProcessed(final String tag, final String preProcessed) {
        this.format.add(TagResolver.resolver(TagResolver.resolver(tag, Tag.preProcessParsed(preProcessed))));
    }

}
