package com.github.breadmoirai.tests.client;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.HandleAbsentArgument;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Required;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.util.Emoji;

@HandleAbsentArgument(EmojiCommand.MissingEmojiAlert.class)
public class EmojiCommand {

    @MainCommand
    public void emoji(CommandEvent event, @Required Emoji e) {
        event.reply(e.getUrl());
    }

    @Command
    public void name(CommandEvent event, @Required Emoji e) {
        event.reply(e.name().replace("_", " "));
    }


    public static class MissingEmojiAlert implements AbsentArgumentHandler {

        @Override
        public void handle(CommandEvent commandEvent, CommandParameter commandParameter) {
            commandEvent.reply("missing parameter: " + commandParameter.getType().getSimpleName());
        }
    }
}
