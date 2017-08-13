package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.core.IModule;
import com.github.breadmoirai.framework.event.CommandEvent;
import com.github.breadmoirai.framework.util.TypeFinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;

public class ModuleCommandWrapper implements ICommand{

    private final MethodHandle constructor;
    private final Type moduleType;

    public ModuleCommandWrapper(Class<? extends ModuleCommand> commandClass) throws NoSuchMethodException, IllegalAccessException {
        constructor = MethodHandles.publicLookup().findConstructor(commandClass,  MethodType.methodType(void.class));
        moduleType = TypeFinder.getTypeArguments(commandClass, ModuleCommand.class)[0];
    }

    @Override
    public void handle(CommandEvent event) throws Throwable {
        ModuleCommand invoke = (ModuleCommand) constructor.invoke();
        IModule module = event.getClient().getModule(moduleType);
        //noinspection unchecked
        invoke.execute(event, module);
    }
}
