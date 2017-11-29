package com.github.breadmoirai.tests.client;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;

public class StaticCommand {

    private static String key;
    private final String response;

    public StaticCommand(String key, String response) {
        StaticCommand.key = key;
        this.response = response;
    }

    @MainCommand
    public void respond(CommandEvent event) {
        event.reply(response);
    }

    @ConfigureCommand
    public static void configure(CommandHandleBuilder handleBuilder) {
        handleBuilder.setKeys(key);
    }
}
