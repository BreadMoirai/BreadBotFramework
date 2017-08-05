package com.github.breadmoirai.bot.framework.core.command;

import com.github.breadmoirai.bot.framework.core.CommandEvent;
import com.github.breadmoirai.bot.framework.core.IModule;
import com.github.breadmoirai.bot.framework.core.Response;
import com.github.breadmoirai.bot.framework.error.AmbiguousCommandMethod;
import com.github.breadmoirai.bot.framework.error.MissingCommandMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractCommand implements ICommand {

    private CommandEvent event;

    @Override
    public void run() {
        execute(event);
    }

    public abstract void execute(CommandEvent event);

    @Override
    final public CommandEvent getEvent() {
        return event;
    }

    @Override
    final public boolean setModules(Map<Type, IModule> moduleTypeMap) {
        return true;
    }

    @Override
    final public void setEvent(CommandEvent event) {
        this.event = event;
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return this.getClass().isAnnotationPresent(annotation);
    }

    public static String[] register(Class<? extends AbstractCommand> commandClass) {
        final List<Method> methods = Arrays.stream(commandClass.getMethods())
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.getReturnType() == Void.TYPE || Response.class.isAssignableFrom(method.getReturnType()))
                .collect(Collectors.toList());
        boolean methodKey = methods.stream().anyMatch(method -> method.isAnnotationPresent(Key.class));
        boolean classKey = commandClass.isAnnotationPresent(Key.class);
        if (classKey && !methodKey) {
            if (methods.size() > 1) {
                throw new AmbiguousCommandMethod(commandClass, methods);
            } else if (methods.isEmpty()) {
                throw new MissingCommandMethod(commandClass);
            }
            else Commands.mapMethodKeys(commandClass, methods.get(0));
        }
    }
}
