package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;

public class BadCommand {

    @MainCommand
    public String bad() {
        throw new RuntimeException("Command Exception");
    }


}
