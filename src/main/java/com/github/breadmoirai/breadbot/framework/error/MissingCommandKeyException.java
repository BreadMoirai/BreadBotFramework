package com.github.breadmoirai.breadbot.framework.error;

import com.github.breadmoirai.breadbot.framework.command.CommandHandle;

public class MissingCommandKeyException extends BreadBotException {
    public MissingCommandKeyException(CommandHandle handle) {
        super(String.format("A key has not been defined for Command: %s",
                handle.getDeclaringClass() == null
                        ? handle.getDeclaringObject().getClass().getSimpleName()
                        : handle.getDeclaringClass().getSimpleName()));
    }
}
