package com.github.breadmoirai.bot.framework.command;

import com.github.breadmoirai.bot.framework.command.arg.CommandArgumentList;

public interface CommandParameter {

    Object map(CommandArgumentList list, CommandParser set);

    Class<?> getType();

    int getFlags();

    int getIndex();

    int getWidth();

    boolean isMustBePresent();
}
