package com.github.breadmoirai.breadbot.framework.internal.command;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.CommandHandle;
import com.github.breadmoirai.breadbot.framework.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.internal.parameter.CommandParser;

import java.util.function.Consumer;

public class CommandRunner implements Runnable {

    private final Object o;
    private final CommandEvent event;
    private final InvokableCommand invokableCommand;
    private final CommandParser parser;
    private final CommandHandle commandHandle;
    private final CommandResultHandler<?> resultHandler;
    private Consumer<Throwable> onException;

    CommandRunner(Object o,
                  CommandEvent event,
                  InvokableCommand invokableCommand,
                  CommandParser parser,
                  CommandHandle commandHandle,
                  CommandResultHandler<?> resultHandler,
                  Consumer<Throwable> onException) {
        this.o = o;
        this.event = event;
        this.invokableCommand = invokableCommand;
        this.parser = parser;
        this.commandHandle = commandHandle;
        //noinspection unchecked
        this.resultHandler = resultHandler;
        this.onException = onException;
    }

    @Override
    public void run() {
        if (parser.mapAll())
            try {
                Object result = invokableCommand.invoke(o, parser.getResults());
                if (result != null) {
                    CommandResultHandler.handleObject(resultHandler, commandHandle, event, result);
                }
            } catch (Throwable throwable) {
                onException.accept(throwable);
            }
    }

}