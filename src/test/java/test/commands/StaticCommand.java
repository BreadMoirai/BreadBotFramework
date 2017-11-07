package test.commands;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.command.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

public class StaticCommand {

    private static String key;
    private final String response;

    public StaticCommand(String key, String response) {
        StaticCommand.key = key;
        this.response = response;
    }

    @MainCommand
    public void respond(CommandEvent event) {
        event.reply(response);
    }

    @ConfigureCommand("respond")
    public static void configure(CommandHandleBuilder handleBuilder) {
        handleBuilder.setKeys(key);
    }
}
