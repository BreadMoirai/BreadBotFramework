package test.commands;

import com.github.breadmoirai.breadbot.framework.Command;
import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.IfNotFound;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Required;
import com.github.breadmoirai.breadbot.framework.internal.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.internal.parameter.MissingArgumentHandler;
import com.github.breadmoirai.breadbot.util.Emoji;

@IfNotFound(EmojiCommand.MissingEmojiAlert.class)
public class EmojiCommand {

    @MainCommand
    public void emoji(CommandEvent event, @Required Emoji e) {
        event.reply(e.getUrl());
    }

    @Command
    public void name(CommandEvent event, @Required Emoji e) {
        event.reply(e.name().replace("_", " "));
    }


    public static class MissingEmojiAlert implements MissingArgumentHandler {

        @Override
        public void handle(CommandEvent commandEvent, CommandParameter commandParameter) {
            commandEvent.reply("missing parameter: " + commandParameter.getType().getSimpleName());
        }
    }
}
