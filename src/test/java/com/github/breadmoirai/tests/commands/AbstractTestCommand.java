package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.command.AbstractCommand;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

public class AbstractTestCommand extends AbstractCommand {

    @Override
    public void onCommand(CommandEvent event) {
        event.reply("abstract");
    }

}
