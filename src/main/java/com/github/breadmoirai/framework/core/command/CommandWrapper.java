package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.core.Response;
import com.github.breadmoirai.framework.error.AmbiguousCommandMethod;
import com.github.breadmoirai.framework.error.MissingCommandMethod;
import com.github.breadmoirai.framework.event.CommandEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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
    private final MethodHandle contructor;

    private final String[] keys;

    public CommandWrapper(Class<?> aClass) throws NoSuchMethodException, IllegalAccessException {
        MethodHandle constructor = MethodHandles.publicLookup().findConstructor(aClass, MethodType.methodType(Void.TYPE));
        this.contructor = constructor;
        this.aClass = aClass;
        keys = register(aClass);
    }

    @Override
    public void handle(CommandEvent event) {
        try {
            Object obj = contructor.invoke();


        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }



    private void execute(Object command, CommandEvent event) {
        final String key = event.getKey().toLowerCase();
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

    }

    private static Stream<String> mapCommandHandles(Class<?> commandClass) {

    }

    public Class<?> getCommandClass() {
        return aClass;
    }
}
