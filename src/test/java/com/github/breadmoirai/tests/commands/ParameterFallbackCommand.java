package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Content;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;

public class ParameterFallbackCommand {

    @MainCommand
    public String fallback(@Content String value) {
        return value;
    }

    @ConfigureCommand
    public static void conf(CommandHandleBuilder command) {
        command.getParameter(0).setDefaultValue("default");
    }

}
