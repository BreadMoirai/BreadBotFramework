package com.github.breadmoirai.bot.framework.event.args;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class UserArgument extends MentionArgument {

    private final User user;

    public UserArgument(JDA jda, Guild guild, TextChannel channel, String arg, User user) {
        super(jda, guild, channel, arg);
        this.user = user;
    }

    @Override
    public boolean isUser() {
        return true;
    }

    @Override
    public User getUser() {
        return user;
    }
}
