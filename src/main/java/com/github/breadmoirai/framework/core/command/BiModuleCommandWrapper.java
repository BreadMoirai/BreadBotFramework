package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.core.IModule;
import com.github.breadmoirai.framework.core.SamuraiClient;
import com.github.breadmoirai.framework.event.CommandEvent;
import com.github.breadmoirai.framework.util.TypeFinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;

public class BiModuleCommandWrapper implements ICommand{

    private final MethodHandle constructor;
    private final Type[] moduleType;

    public BiModuleCommandWrapper(Class<? extends BiModuleCommand> commandClass) throws NoSuchMethodException, IllegalAccessException {
        constructor = MethodHandles.publicLookup().findConstructor(commandClass,  MethodType.methodType(void.class));
        moduleType = TypeFinder.getTypeArguments(commandClass, BiModuleCommand.class);
    }

    @Override
    public void handle(CommandEvent event) throws Throwable {
        BiModuleCommand invoke = (BiModuleCommand) constructor.invoke();
        SamuraiClient client = event.getClient();
        IModule module1 = client.getModule(moduleType[0]);
        IModule module2 = client.getModule(moduleType[1]);
        //noinspection unchecked
        invoke.execute(event, module1, module2);
    }
}
