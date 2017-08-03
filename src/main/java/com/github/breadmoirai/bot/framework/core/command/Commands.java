package com.github.breadmoirai.bot.framework.core.command;

import com.github.breadmoirai.bot.framework.util.DiscordPatterns;
import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Commands {
    enum Type {
        SINGLE, MULTI, SUB
    }

    static final SimpleLog LOG = SimpleLog.getLog("Command");

    private static final Map<String, Pair<MethodHandle, Annotation[]>> METHODS = new HashMap<>();

    static boolean checkKey(Class<?> clazz, String key, boolean noSpace) {
        if (noSpace && DiscordPatterns.WHITE_SPACE.matcher(key).find()) {
            Commands.LOG.warn("Invalid Key: '" + key + "' for " + clazz.getName());
            return false;
        }
        return !METHODS.containsKey(key);
    }

    static Optional<MethodHandle> getHandle(String key) {
        final Pair<MethodHandle, Annotation[]> methodHandlePair = METHODS.get(key);
        return methodHandlePair != null ? Optional.of(methodHandlePair.getLeft()) : Optional.empty();
    }

    static boolean isAnnotatedWith(String key, Class<? extends Annotation> annotation) {
        final Pair<MethodHandle, Annotation[]> methodHandlePair = METHODS.get(key);
        return methodHandlePair != null && Arrays.stream(methodHandlePair.getRight()).anyMatch(a -> annotation.isAssignableFrom(a.annotationType()));
    }

    static Stream<? extends String> mapMethodKeys(Class<?> commandClass, Method method) {
        try {
            final Pair<MethodHandle, Annotation[]> handlePair = getMethodHandlePair(method);
            return Arrays.stream(method.getAnnotation(Key.class).value())
                    .map(String::toLowerCase)
                    .filter(s -> Commands.checkKey(commandClass, s, true))
                    .peek(key -> METHODS.put(key, handlePair));
        } catch (IllegalAccessException ignored) {
            return Stream.empty();
        }
    }

    static Stream<? extends String> mapSubMethodKeys(Class<?> commandClass, Method method, String[] primaryKey) {
        try {
            final Pair<MethodHandle, Annotation[]> handlePair = getMethodHandlePair(method);
            return Arrays.stream(method.getAnnotation(Key.class).value())
                    .map(String::toLowerCase)
                    .flatMap(s -> Arrays.stream(primaryKey).map(pk -> s.isEmpty() ? pk : pk + " " + s))
                    .filter(s -> Commands.checkKey(commandClass, s, false))
                    .peek(key -> METHODS.put(key, handlePair));
        } catch (IllegalAccessException ignored) {
            return Stream.empty();
        }
    }

    @NotNull
    private static Pair<MethodHandle, Annotation[]> getMethodHandlePair(Method method) throws IllegalAccessException {
        final Annotation[] annotations = method.getAnnotations();
        final MethodHandle methodHandle = MethodHandles.publicLookup().unreflect(method);
        return Pair.of(methodHandle, annotations);
    }
}
