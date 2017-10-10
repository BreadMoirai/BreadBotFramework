package com.github.breadmoirai.bot.framework.command.buildernew;

import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.function.BiConsumer;

public class CommandRunner implements Runnable {

    private final Object o;
    private final CommandEvent event;
    private final BiConsumer<Object, CommandEvent> biConsumer;

    public CommandRunner(Object o, CommandEvent event, BiConsumer<Object, CommandEvent> biConsumer) {
        this.o = o;
        this.event = event;
        this.biConsumer = biConsumer;
    }

    public Object getCommandObject() {
        return o;
    }

    public CommandEvent getCommandEvent() {
        return event;
    }

    @Override
    public void run() {
        biConsumer.accept(o, event);
    }

}