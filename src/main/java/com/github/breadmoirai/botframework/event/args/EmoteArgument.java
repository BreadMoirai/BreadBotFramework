package com.github.breadmoirai.botframework.event.args;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    @Override
    public Emote getEmote() {
        return emote;
    }
}
