package com.github.breadmoirai.breadbot.framework.internal.parameter.arguments;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TextChannelArgument extends MentionArgument {

    private final TextChannel channel;

    public TextChannelArgument(CommandEvent event, String s, TextChannel textChannel) {
        super(event, s);
        this.channel = textChannel;
    }

    @Override
    public boolean isTextChannel() {
        return true;
    }

    @Override
    public boolean isValidTextChannel() {
        return true;
    }

    @NotNull
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
