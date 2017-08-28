package com.github.breadmoirai.bot.framework.arg.impl;

import com.github.breadmoirai.bot.framework.arg.impl.MentionArgument;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class EmoteArgument extends MentionArgument {

    private final Emote emote;

    public EmoteArgument(JDA jda, Guild guild, TextChannel channel, String s, Emote emote) {
        super(jda, guild, channel, s);
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
