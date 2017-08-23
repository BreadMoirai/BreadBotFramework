package com.github.breadmoirai.bot.framework.event.args.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TextChannelArgument extends MentionArgument {
    private final TextChannel channel;

    public TextChannelArgument(JDA jda, Guild guild, TextChannel textChannel, String arg, TextChannel channel) {
        super(jda, guild, textChannel, arg);
        this.channel = channel;
    }

    @Override
    public boolean isTextChannel() {
        return true;
    }

    @Override
    public boolean isValidTextChannel() {
        return true;
    }

    @Nullable
    @Override
    public TextChannel getTextChannel() {
        return channel;
    }

    @NotNull
    @Override
    public Optional<TextChannel> findTextChannel() {
        return Optional.of(channel);
    }

    @NotNull
    @Override
    public List<TextChannel> searchTextChannels() {
        return Collections.singletonList(channel);
    }
}
