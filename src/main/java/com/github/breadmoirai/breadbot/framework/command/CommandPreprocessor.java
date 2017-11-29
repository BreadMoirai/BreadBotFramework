package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandProcessStack;

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
    public void process(Object commandObj, CommandHandle targetHandle, CommandEvent event, CommandProcessStack processQueue) {
        getFunction().process(commandObj, targetHandle, event, processQueue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandPreprocessor that = (CommandPreprocessor) o;

        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        return function != null ? function.equals(that.function) : that.function == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (function != null ? function.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CommandPreprocessor[" + identifier + ']';
    }
}
