package com.github.breadmoirai.bot.framework.command.arg.impl;

import com.github.breadmoirai.bot.framework.event.CommandEvent;
import com.github.breadmoirai.bot.util.Emoji;
import org.jetbrains.annotations.NotNull;

public class EmojiArgument extends MentionArgument {

    private final Emoji emoji;

    public EmojiArgument(CommandEvent event, String s, Emoji emoji) {
        super(event, s);
        this.emoji = emoji;
    }

    @Override
    public boolean isEmoji() {
        return true;
    }

    @NotNull
    @Override
    public Emoji getEmoji() {
        return emoji;
    }
}
