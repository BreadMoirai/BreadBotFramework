package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;

public class BadCommand {

    public BadCommand() {
        throw new RuntimeException("Very Bad");
    }

    @MainCommand
    public String bad() {
        return "This should never appear";
    }
}
