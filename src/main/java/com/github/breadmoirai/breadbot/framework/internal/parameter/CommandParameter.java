package com.github.breadmoirai.breadbot.framework.internal.parameter;

import com.github.breadmoirai.breadbot.framework.CommandArgumentList;

public interface CommandParameter {

    default Object map(CommandParser parser) {
        return map(parser.getArgumentList(), parser);
    }

    Object map(CommandArgumentList list, CommandParser parser);

    Class<?> getType();

    int getFlags();

    int getIndex();

    int getWidth();

    boolean isMustBePresent();
}
