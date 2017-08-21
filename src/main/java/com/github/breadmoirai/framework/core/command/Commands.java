package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.event.CommandEvent;
import com.github.breadmoirai.framework.core.Response;
import com.github.breadmoirai.framework.util.DiscordPatterns;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Commands {

    public static final SimpleLog BUILDLOG = SimpleLog.getLog("CommandBuilder");
    public static final SimpleLog LOG = SimpleLog.getLog("Command");

    private static final Map<String, CommandMethod> METHODS = new HashMap<>();

    static boolean checkKey(Class<?> clazz, String key, boolean noSpace) {
        if (noSpace && DiscordPatterns.WHITE_SPACE.matcher(key).find()) {
            Commands.LOG.warn("Invalid Key: '" + key + "' for " + clazz.getName());
            return false;
        }
        return !METHODS.containsKey(key);
    }

    static boolean isAnnotatedWith(String key, Class<? extends Annotation> annotation) {
        final CommandMethod commandMethod = METHODS.get(key);
        return commandMethod != null && commandMethod.isAnnotationPresent(annotation);
    }

    static Stream<String> mapMethodKeys(Class<?> commandClass, Method method) {
        try {
            final CommandMethod commandMethod = new CommandMethod(commandClass, method);
            return commandMethod.getKeys()
                    .filter(s -> Commands.checkKey(commandClass, s, true))
                    .peek(key -> METHODS.put(key, commandMethod));
        } catch (IllegalAccessException ignored) {
            return Stream.empty();
        }
    }

    static void invokeCommand(String key, ICommand command, CommandEvent event, Object... args) {
        final CommandMethod commandMethod = METHODS.get(key);
        if (commandMethod != null) {
            try {
                // todo maybe map asSpreader in methodMapping instead of during invocation? check other use cases
                final Object invoke = commandMethod.getHandle().asSpreader(Object.class, args.length).invoke(command, event, args);
                if (invoke instanceof Response) {
                    final Response response = (Response) invoke;
                    response.base(event);
                    response.send();
                }
            } catch (Throwable throwable) {
                Commands.LOG.fatal(throwable);
            }
        }
    }
}