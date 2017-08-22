package com.github.breadmoirai.botframework.core.command;

import com.github.breadmoirai.botframework.core.IModule;
import com.github.breadmoirai.botframework.core.SamuraiClient;
import com.github.breadmoirai.botframework.event.CommandEvent;
import com.github.breadmoirai.botframework.util.TypeFinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

public class BiModuleCommandWrapper extends CommandWrapper {

    private final Supplier<BiModuleCommand> supplier;
    private final Type[] moduleType;

    private final String toString;

    public BiModuleCommandWrapper(Class<? extends BiModuleCommand> commandClass) throws NoSuchMethodException, IllegalAccessException {
        super(commandClass);
        MethodHandle constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class));
        supplier = () -> {
            try {
                return (BiModuleCommand) constructor.invoke();
            } catch (Throwable throwable) {
                Commands.LOG.fatal(throwable);
                return null;
            }
        };
        moduleType = TypeFinder.getTypeArguments(commandClass, ModuleCommand.class);
        toString = String.format("BiModuleCommand[%s]<%s, %s>", commandClass.getSimpleName(), moduleType[0].getTypeName(), moduleType[1].getTypeName());
    }

    public BiModuleCommandWrapper(BiModuleCommand commandObj) {
        super(commandObj.getClass());
        supplier = () -> commandObj;
        final Class<? extends BiModuleCommand> commandClass = commandObj.getClass();
        moduleType = TypeFinder.getTypeArguments(commandClass, ModuleCommand.class);
        toString = String.format("BiModuleCommand[%s]<%s, %s>", commandClass.getSimpleName(), moduleType[0].getTypeName(), moduleType[1].getTypeName());
    }

    @Override
    public void handle(CommandEvent event) throws Throwable {
        BiModuleCommand invoke = supplier.get();
        SamuraiClient client = event.getClient();
        IModule module1 = client.getModule(moduleType[0]);
        IModule module2 = client.getModule(moduleType[1]);
        //noinspection unchecked
        invoke.execute(event, module1, module2);
    }

    @Override
    public String toString() {
        return toString;
    }
}
