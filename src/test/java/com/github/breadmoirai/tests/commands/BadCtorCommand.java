package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;

public class BadCtorCommand {

    public BadCtorCommand() {
        throw new RuntimeException("Constructor Exception");
    }

    @MainCommand
    public String bad() {
        return null;
    }


}
