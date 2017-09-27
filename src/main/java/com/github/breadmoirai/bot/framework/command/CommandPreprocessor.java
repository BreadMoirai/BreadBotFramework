package com.github.breadmoirai.bot.framework.command;

import com.github.breadmoirai.bot.framework.event.CommandEvent;

public class CommandPreprocessor implements CommandPreprocessorFunction {
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

    @Override
    public void process(Object commandObj, CommandHandle targetHandle, CommandEvent event, CommandProcessorStack processQueue) {
        getFunction().process(commandObj, targetHandle, event, processQueue);
    }
}
