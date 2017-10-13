package com.github.breadmoirai.breadbot.framework.command.parameter;

import com.github.breadmoirai.breadbot.framework.command.CommandArgumentList;

public interface CommandParameter {

    Object map(CommandArgumentList list, CommandParser set);

    Class<?> getType();

    int getFlags();

    int getIndex();

    int getWidth();

    boolean isMustBePresent();
}
