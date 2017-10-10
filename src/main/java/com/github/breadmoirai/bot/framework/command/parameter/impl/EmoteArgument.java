package com.github.breadmoirai.bot.framework.command.parameter.impl;

import com.github.breadmoirai.bot.framework.event.CommandEvent;
import net.dv8tion.jda.core.entities.Emote;
import org.jetbrains.annotations.NotNull;

public class EmoteArgument extends MentionArgument {

    private final Emote emote;

    public EmoteArgument(CommandEvent event, String s, Emote emote) {
        super(event, s);
        this.emote = emote;
    }

    @Override
    public boolean isEmote() {
        return true;
    }

    @NotNull
    @Override
    public Emote getEmote() {
        return emote;
    }
}
