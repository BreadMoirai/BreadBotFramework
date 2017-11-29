package com.github.breadmoirai.breadbot.framework.internal.parameter.argument;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import net.dv8tion.jda.core.entities.User;
import org.jetbrains.annotations.NotNull;

public class UserArgument extends MentionArgument {

    private final User user;

    public UserArgument(CommandEvent event, String arg, User user) {
        super(event, arg);
        this.user = user;
    }

    @Override
    public boolean isUser() {
        return true;
    }

    @Override
    public boolean isValidUser() {
        return true;
    }

    @NotNull
    @Override
    public User getUser() {
        return user;
    }
}
