package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;

public class BadPrepCommand {

    @MainCommand
    public String bad() {
        return null;
    }

    @ConfigureCommand
    public static void configure(CommandHandleBuilder builder) {
        builder.addPreprocessorFunction("thrower", (commandObj, targetHandle, event, processorStack) -> {
            throw new RuntimeException("Preprocessor Exception");
        });
    }

}
