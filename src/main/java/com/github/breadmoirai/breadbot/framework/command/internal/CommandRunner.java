package com.github.breadmoirai.breadbot.framework.command.internal;

import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParser;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.function.Consumer;

public class CommandRunner implements Runnable {

    private final Object o;
    private final CommandEvent event;
    private final InvokableCommand invokableCommand;
    private final CommandParser parser;
    private Consumer<Throwable> onException;

    public CommandRunner(Object o,
                         CommandEvent event,
                         InvokableCommand invokableCommand,
                         CommandParser parser,
                         Consumer<Throwable> onException) {
        this.o = o;
        this.event = event;
        this.invokableCommand = invokableCommand;
        this.parser = parser;
        this.onException = onException;
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
            try {
                invokableCommand.invoke(o, parser.getResults());
            } catch (Throwable throwable) {
                onException.accept(throwable);
            }
    }

}