package com.github.breadmoirai.breadbot.framework.error;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AmbiguousCommandMethod extends CommandInitializationException {
    public AmbiguousCommandMethod(Class<?> command, List<Method> methods) {
        super(String.format("Command Class: %s does not explicitly mark any methods as a command.%n\tCandidate Methods: %s",
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
