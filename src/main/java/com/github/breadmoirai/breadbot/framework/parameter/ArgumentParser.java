package com.github.breadmoirai.breadbot.framework.parameter;

import com.github.breadmoirai.breadbot.framework.event.CommandArgumentList;

@FunctionalInterface
public interface ArgumentParser {

    Object parse(CommandParameter parameter, CommandArgumentList list, CommandParser parser);

}
