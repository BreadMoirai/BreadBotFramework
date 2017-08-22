package com.github.breadmoirai.botframework.error;

public class MissingCommandMethod extends CommandInitializationException {
    public MissingCommandMethod(Class<?> command) {
        super(String.format("Command Class: %s does not a have compatible method declaration for execution",
                command.getName()));
    }
}
