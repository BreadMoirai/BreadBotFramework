package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Width;

import java.time.OffsetDateTime;

public class DateCommand {

    @MainCommand
    public String t(@Width(-1) OffsetDateTime time) {
        return time != null ? time.toString() : null;
    }
}
