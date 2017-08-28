package com.github.breadmoirai.bot.framework.arg.impl;

import com.github.breadmoirai.bot.framework.arg.impl.MentionArgument;
import com.github.breadmoirai.bot.util.Emoji;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class EmojiArgument extends MentionArgument {
    private final Emoji emoji;

    public EmojiArgument(JDA jda, Guild guild, TextChannel channel, String s, Emoji emoji) {
        super(jda, guild, channel, s);
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
