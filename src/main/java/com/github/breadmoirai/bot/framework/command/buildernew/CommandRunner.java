package com.github.breadmoirai.bot.framework.command.buildernew;

import com.github.breadmoirai.bot.framework.command.parameter.CommandParser;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.function.BiConsumer;

public class CommandRunner implements Runnable {

    private final Object o;
    private final CommandEvent event;
    private final BiConsumer<Object, Object[]> biConsumer;
    private final CommandParser parser;

    public CommandRunner(Object o, CommandEvent event, BiConsumer<Object, Object[]> biConsumer, CommandParser parser) {
        this.o = o;
        this.event = event;
        this.biConsumer = biConsumer;
        this.parser = parser;
    }

    public Object getCommandObject() {
        return o;
    }

    public CommandEvent getCommandEvent() {
        return event;
    }

    @Override
    public void run() {
        if (parser.mapAll())
            biConsumer.accept(o, parser.getResults());
    }

}