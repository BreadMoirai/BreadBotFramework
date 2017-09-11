package com.github.breadmoirai.bot.framework.command;

import com.github.breadmoirai.bot.framework.command.impl.CommandHandle;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.ArrayDeque;
import java.util.Collection;

public class CommandProcessQueue extends ArrayDeque<CommandPreprocessor> {

    private final Object object;
    private final CommandHandle targetHandle;
    private final CommandEvent event;
    private final Runnable onEnd;

    public CommandProcessQueue(Object object, CommandHandle targetHandle, CommandEvent event, Collection<CommandPreprocessor> preprocessors, Runnable onEnd) {
        super(preprocessors);
        this.object = object;
        this.targetHandle = targetHandle;
        this.event = event;
        this.onEnd = onEnd;
    }

    public Object getCommandObject() {
        return object;
    }

    public CommandHandle getTargetHandle() {
        return targetHandle;
    }

    public CommandEvent getEvent() {
        return event;
    }

    public void runNext() {
        if (!this.isEmpty())
            this.pop().getFunction().process(object, targetHandle, event, this);
        else {
            onEnd.run();
        }
    }
}
