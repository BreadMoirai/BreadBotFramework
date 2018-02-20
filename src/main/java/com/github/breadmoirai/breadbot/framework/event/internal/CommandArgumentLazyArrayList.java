package com.github.breadmoirai.breadbot.framework.event.internal;

import com.github.breadmoirai.breadbot.framework.event.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;

import java.util.RandomAccess;

public class CommandArgumentLazyArrayList extends CommandArgumentList implements RandomAccess {

    private final String[] strings;
    private final CommandArgument[] arguments;
    private final CommandArgumentFactory factory;

    public CommandArgumentLazyArrayList(String[] strings, CommandEvent event) {
        super(event);
        this.strings = strings;
        this.arguments = new CommandArgument[strings.length];
        this.factory = new CommandArgumentFactory(event);
    }

    @Override
    public CommandArgument get(int index) {
        final CommandArgument argument = arguments[index];
        if (argument == null) {
            final CommandArgument parse = factory.parse(strings[index]);
            arguments[index] = parse;
            return parse;
        } else {
            return argument;
        }
    }

    @Override
    public int size() {
        return strings.length;
    }

}
