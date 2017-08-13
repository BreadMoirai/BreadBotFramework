package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.event.CommandEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SimpleCommandWrapper implements ICommand {

    private final MethodHandle constructor;

    public SimpleCommandWrapper(Class<? extends ICommand> commandClass) throws NoSuchMethodException, IllegalAccessException {
        constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class));
    }

    @Override
    public void handle(CommandEvent event) throws Throwable {
        ICommand invoke = (ICommand) constructor.invoke();
        //noinspection unchecked
        invoke.handle(event);
    }
}
