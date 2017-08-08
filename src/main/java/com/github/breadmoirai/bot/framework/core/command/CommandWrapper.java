package com.github.breadmoirai.bot.framework.core.command;

import com.github.breadmoirai.bot.framework.core.CommandEvent;
import com.github.breadmoirai.bot.framework.core.Response;
import com.github.breadmoirai.bot.framework.error.AmbiguousCommandMethod;
import com.github.breadmoirai.bot.framework.error.MissingCommandMethod;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandWrapper implements ICommand {

    private final Map<String, CommandHandle> handleMap = new HashMap<>();

    private final Class<?> aClass;

    private transient CommandEvent event;
    private final String[] keys;

    public CommandWrapper(Class<?> aClass) throws NoSuchMethodException {
        this.aClass = aClass;
        final Constructor<?> constructor = aClass.getConstructor();
        keys = register(aClass);
    }

    @Override
    public void run() {
        aClass.ne
        execute(event);
        event = null;
    }

    private void execute(CommandEvent event) {
        final String key = event.getKey().toLowerCase();
    }

    @Override
    final public CommandEvent getEvent() {
        return event;
    }

    @Override
    final public void setEvent(CommandEvent event) {
        this.event = event;
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return this.getClass().isAnnotationPresent(annotation);
    }

    @SuppressWarnings("unchecked")
    public String[] register(Class<?> commandClass) {
        final SimpleLog log = SimpleLog.getLog("CommandBuilder");
        final String[] keys;

        final List<Method> methods = Arrays.stream(commandClass.getMethods())
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.getReturnType() == Void.TYPE || Response.class.isAssignableFrom(method.getReturnType()))
                .collect(Collectors.toList());
        boolean classKey = commandClass.isAnnotationPresent(Command.class);

        final List<Class<?>> classList = Arrays.stream(commandClass.getClasses())
                .filter(aClass -> aClass.isAnnotationPresent(Command.class))
                .collect(Collectors.toList());
        boolean innerClassKey = !classList.isEmpty();
        boolean methodKey = methods.stream().anyMatch(method -> method.isAnnotationPresent(Command.class));
        if (classKey && !methodKey) {
            if (methods.size() > 1) {
                throw new AmbiguousCommandMethod(commandClass, methods);
            } else if (methods.isEmpty()) {
                throw new MissingCommandMethod(commandClass);
            } else Commands.mapMethodKeys(commandClass, methods.get(0));
        }

        return keys;
    }

    private static Stream<String> mapCommandHandles(Class<?> commandClass) {

    }

    public Class<?> getCommandClass() {
        return aClass;
    }
}
