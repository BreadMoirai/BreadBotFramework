package com.github.breadmoirai.botframework.core.command;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

public class CommandMethod {

    private final Class<?> commandClass;
    private final Method method;
    private MethodHandle handle;

    public CommandMethod(Class<?> commandClass, Method method) throws IllegalAccessException {
        this.commandClass = commandClass;
        this.method = method;
        handle = MethodHandles.publicLookup().unreflect(method);
    }

    public MethodHandle getHandle() {
        return handle;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return method.isAnnotationPresent(annotation);
    }

    public Stream<String> getKeys() {
        final Command classKey = commandClass.getAnnotation(Command.class);
        final Command methodKey = method.getAnnotation(Command.class);
        if (classKey != null && methodKey != null) {
            return Arrays.stream(classKey.value())
                    .flatMap(s -> Arrays.stream(methodKey.value())
                            .map(ss -> s + " " + ss)
                            .map(String::toLowerCase));
        } else if (classKey != null) {
            return Arrays.stream(classKey.value()).map(String::toLowerCase);
        } else if (methodKey != null) {
            return Arrays.stream(methodKey.value()).map(String::toLowerCase);
        } else {
            return Stream.empty();
        }
    }
}
