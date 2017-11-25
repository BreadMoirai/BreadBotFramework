package test.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;

public class HelpCommand {

    @Command
    public String help() {
        return "sure";
    }

    public static class MeCommand {

        @MainCommand
        public String me() {
            return "you";
        }

        @Command
        public String help() {
            return "maybe";
        }
    }

}
