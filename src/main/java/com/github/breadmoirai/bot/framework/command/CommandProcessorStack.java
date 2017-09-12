package com.github.breadmoirai.bot.framework.command;

import com.github.breadmoirai.bot.framework.command.impl.CommandHandle;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * This is an ArrayDeque. Intended to be used as a stack. Generally the only important method is {@link #runNext()}
 */
public class CommandProcessorStack extends ArrayDeque<CommandPreprocessor> {

    private final Object object;
    private final CommandHandle targetHandle;
    private final CommandEvent event;
    private final Runnable onEnd;

    public CommandProcessorStack(Object object, CommandHandle targetHandle, CommandEvent event, Collection<CommandPreprocessor> preprocessors, Runnable onEnd) {
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

    /**
     * It is generally recommended to use this method to continue operation. Calling this method when this stack is empty will run the command.
     */
    public void runNext() {
        if (!this.isEmpty())
            this.pop().process(object, targetHandle, event, this);
        else {
            onEnd.run();
        }
    }
}
