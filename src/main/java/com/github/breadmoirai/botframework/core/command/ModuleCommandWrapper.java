package com.github.breadmoirai.botframework.core.command;

import com.github.breadmoirai.botframework.core.IModule;
import com.github.breadmoirai.botframework.event.CommandEvent;
import com.github.breadmoirai.botframework.util.TypeFinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

public class ModuleCommandWrapper extends CommandWrapper {

    private final Supplier<ModuleCommand> supplier;
    private final Type moduleType;

    private final String toString;

    public ModuleCommandWrapper(Class<? extends ModuleCommand> commandClass) throws NoSuchMethodException, IllegalAccessException {
        super(commandClass);
        MethodHandle constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class));
        supplier = () -> {
            try {
                return (ModuleCommand) constructor.invoke();
            } catch (Throwable throwable) {
                Commands.LOG.fatal(throwable);
                return null;
            }
        };
        moduleType = TypeFinder.getTypeArguments(commandClass, ModuleCommand.class)[0];
        toString = String.format("ModuleCommand[%s]<%s>", commandClass.getSimpleName(), moduleType.getTypeName());
    }

    public ModuleCommandWrapper(ModuleCommand commandObj) {
        super(commandObj.getClass());
        supplier = () -> commandObj;
        final Class<? extends ModuleCommand> commandClass = commandObj.getClass();
        moduleType = TypeFinder.getTypeArguments(commandClass, ModuleCommand.class)[0];
        toString = String.format("ModuleCommand[%s]<%s>", commandClass.getSimpleName(), moduleType.getTypeName());
    }

    @Override
    public void handle(CommandEvent event) throws Throwable {
        final ModuleCommand invoke = supplier.get();
        if (invoke == null) {
            return;
        }
        IModule module = event.getClient().getModule(moduleType);
        //noinspection unchecked
        invoke.execute(event, module);
    }

    @Override
    public String toString() {
        return toString;
    }
}
