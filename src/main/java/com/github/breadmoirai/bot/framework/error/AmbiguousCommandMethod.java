package com.github.breadmoirai.bot.framework.error;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AmbiguousCommandMethod extends Error {
    public AmbiguousCommandMethod(Class<?> command, List<Method> methods) {
        super(String.format("CommandClass: %s has ambiguous method declaration for command execution with methods: %s",
                command.getName(),
                methods.stream()
                        .map(method -> String.format("%s(%s)",
                                method.getName(),
                                Arrays.stream(method.getParameterTypes())
                                        .map(Class::getSimpleName)
                                        .collect(Collectors.joining(", "))))
                        .collect(Collectors.joining(", "))));
    }
}
