package com.github.breadmoirai.breadbot.framework.parameter;

@FunctionalInterface
public interface ArgumentParser {

    Object parse(CommandParameter parameter, CommandArgumentList list, CommandParser parser);

}
