package com.github.breadmoirai.bot.framework.command;

public interface CommandParameter {

    Object map(CommandArgumentList list, CommandParser set);

    Class<?> getType();

    int getFlags();

    int getIndex();

    int getWidth();

    boolean isMustBePresent();
}
