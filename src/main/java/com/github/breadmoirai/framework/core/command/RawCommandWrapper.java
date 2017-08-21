package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.event.CommandEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

public class RawCommandWrapper extends CommandWrapper {

    private final Supplier<ICommand> supplier;

    private final String toString;

    public RawCommandWrapper(Class<? extends ICommand> commandClass) throws NoSuchMethodException, IllegalAccessException {
        super(commandClass);

        final MethodHandle constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class));
        supplier = () -> {
            try {
                return ((ICommand) constructor.invoke());
            } catch (Throwable throwable) {
                Commands.LOG.fatal(throwable);
                return null;
            }
        };

        toString = String.format("SimpleCommand[%s]", commandClass.getSimpleName());
    }

    public RawCommandWrapper(ICommand commandObj) {
        super(commandObj.getClass());

        supplier = () -> commandObj;

        toString = String.format("SimpleCommand[%s]", commandObj.getClass().getSimpleName());
    }

    @Override
    public void handle(CommandEvent event) throws Throwable {
        ICommand invoke = supplier.get();
        //noinspection unchecked
        invoke.handle(event);
    }

    @Override
    public String toString() {
        return toString;
    }
}
