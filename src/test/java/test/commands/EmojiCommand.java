package test.commands;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.DefaultCommand;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.command.parameter.IfNotFound;
import com.github.breadmoirai.breadbot.framework.command.parameter.MissingArgumentHandler;
import com.github.breadmoirai.breadbot.framework.command.parameter.Required;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.util.Emoji;

@Command
@IfNotFound(EmojiCommand.MissingEmojiAlert.class)
public class EmojiCommand {

    @DefaultCommand
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
