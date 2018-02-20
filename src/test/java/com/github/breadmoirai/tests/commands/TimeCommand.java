package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;

import java.time.Duration;

public class TimeCommand {

    @MainCommand
    public String d(Duration duration) {
        return duration.toString();
    }
}
