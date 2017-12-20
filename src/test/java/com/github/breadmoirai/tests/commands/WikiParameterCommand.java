package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.annotation.Name;
import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Index;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Required;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Width;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;

public class WikiParameterCommand {

    @Command("ex")
    @Name("example")
    public String exampleCommand(@Required @Index(-1) Integer lint,
                                 @Index(3) String third,
                                 @Width(0) String start) {
        return "lint=" + lint +
                ", third=" + third +
                ", start=" + start;

    }

    @ConfigureCommand("example")
    public static void configureCommand(CommandHandleBuilder command) {
        command.getParameter(0)
                .setOnAbsentArgument((event, param) -> {
                    event.replyFormat("Error: required [%s] but not found", param.getName());
                });
    }

}
