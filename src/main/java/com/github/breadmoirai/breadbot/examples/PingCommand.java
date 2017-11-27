package com.github.breadmoirai.breadbot.examples;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import com.github.breadmoirai.breadbot.framework.response.Responses;

public class PingCommand {

    @Command
    public CommandResponse ping() {
        return Responses.of("pong").uponSuccess()
    }

    @Command
    public void ping2(CommandEvent event) {

    }
}
