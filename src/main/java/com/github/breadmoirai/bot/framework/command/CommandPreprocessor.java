package com.github.breadmoirai.bot.framework.command;

public class CommandPreprocessor {
    private final String identifier;
    private final CommandPreprocessorFunction function;

    public CommandPreprocessor(String identifier, CommandPreprocessorFunction function) {
        this.identifier = identifier;
        this.function = function;
    }

    public String getIdentifier() {
        return identifier;
    }

    public CommandPreprocessorFunction getFunction() {
        return function;
    }
}
