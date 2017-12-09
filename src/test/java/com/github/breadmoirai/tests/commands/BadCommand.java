package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;

public class BadCommand {

    public static boolean preException = false;
    public static boolean ctorException = false;

    public BadCommand() {
        if (ctorException) {
            ctorException = false;
            throw new RuntimeException("Constructor Exception");
        }
    }

    @MainCommand
    public String bad() {
        throw new RuntimeException("Command Exception");
    }

    @ConfigureCommand
    public static void configure(CommandHandleBuilder builder) {
        if (preException) {
            builder.addPreprocessorFunction("thrower", (commandObj, targetHandle, event, processorStack) -> {
                throw new RuntimeException("Preprocessor Exception");
            });
            preException = false;
        }
    }

}
