/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.CommandPropertyMap;
import com.github.breadmoirai.breadbot.framework.command.impl.*;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParameterFunctionImpl;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.util.ExceptionalSupplier;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandleBuilderFactoryImpl implements CommandHandleBuilderFactory {

    private final BreadBotClientBuilder clientBuilder;

    public CommandHandleBuilderFactoryImpl(BreadBotClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public void addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(onCommand));
    }

    @Override
    public void addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandClass));
    }

    @Override
    public void addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandObject));
    }

    @Override
    public void addCommand(Supplier<Object> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandSupplier));
    }

    @Override
    public void addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        List<CommandHandleBuilder> commands = createCommands(packageName);
        commands.forEach(configurator);
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Consumer<CommandEvent> onCommand) {
        CommandHandleBuilderImpl commandHandleBuilder = fromConsumer(onCommand);
        return commandHandleBuilder;
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Supplier<Object> commandSupplier) {
        Object o = commandSupplier.get();
        return fromObjectSupplier(o, commandSupplier);
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Class<?> commandClass) {
        return fromObjectClass(commandClass, null);
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Object commandObject) {
        return fromObject(commandObject);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(String packageName) {
        List<CommandHandleBuilderImpl> builders = new ArrayList<>();
        final Reflections reflections = new Reflections(packageName);
        final Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        for (Class<?> commandClass : classes) {
            final int mod = commandClass.getModifiers();
            if (commandClass.isInterface()
                    || commandClass.isSynthetic()
                    || commandClass.isAnonymousClass()
                    || commandClass.isArray()
                    || commandClass.isAnnotation()
                    || commandClass.isEnum()
                    || commandClass.isPrimitive()
                    || commandClass.isLocalClass()
                    || commandClass.isMemberClass()
                    || Modifier.isAbstract(mod)
                    || Modifier.isPrivate(mod)
                    || Modifier.isProtected(mod))
                continue;
            Stream<GenericDeclaration> classStream = Stream.concat(Stream.concat(Stream.of(commandClass), Arrays.stream(commandClass.getMethods())), Arrays.stream(commandClass.getClasses()));
            boolean hasCommandAnnotation = classStream.map(AnnotatedElement::getAnnotations)
                    .flatMap(Arrays::stream)
                    .map(Annotation::annotationType)
                    .anyMatch(aClass -> aClass == Command.class);
            if (!hasCommandAnnotation) continue;
            builders.add((CommandHandleBuilderImpl) createCommand(commandClass));
        }
        return new ArrayList<>(builders);
    }

    private <T> CommandHandleBuilderInternal fromObject(T object) {
        @SuppressWarnings("unchecked") Class<T> aClass = (Class<T>) object.getClass();
        return fromObjectClass(aClass, object);
    }

    private <T> CommandHandleBuilderImpl fromObjectSupplier(T commandObj, Supplier<Object> commandSupplier) {
        @SuppressWarnings("unchecked") Class<T> aClass = (Class<T>) commandObj.getClass();
        return fromClass(aClass, null, commandSupplier, null);
    }

    private <T> CommandHandleBuilderImpl fromObjectClass(Class<T> commandClass, @Nullable T object) {
        Supplier<Object> supplier = object != null ? () -> object : null;
        return fromClass(object, commandClass, null, supplier, null);
    }

    private CommandHandleBuilderImpl fromConsumer(Consumer<CommandEvent> onCommand) {
        return new CommandHandleBuilderImpl(null, null,
                clientBuilder,
                new CommandObjectFactory(() -> onCommand),
                new CommandParameterBuilder[]{new CommandParameterBuilderSpecificImpl("This parameter of type CommandEvent is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent()))},
                (o, objects) -> onCommand.accept(((CommandEvent) objects[0])));
    }

    private List<CommandHandleBuilderImpl> fromClassMethods(Class<?> commandClass, Supplier<Object> supplier, CommandPropertyMapImpl defaultPropertyMap) throws BreadBotException {
        final CommandObjectFactory commandSupplier = createCommandFactory(commandClass, null, supplier);

        final CommandPropertyMapImpl propertyMap = createPropertyMap(commandClass, defaultPropertyMap);

        Stream<CommandHandleBuilderImpl> stream1 = getCommandMethodStream(commandClass)
                .map(method -> {
                    CommandPropertyMapImpl map = new CommandPropertyMapImpl(propertyMap, method.getAnnotations());
                    Pair<CommandParameterBuilder[], InvokableCommand> invokableCommandPair = mapMethod(method, map);
                    return new CommandHandleBuilderImpl(null, method, clientBuilder, commandSupplier, invokableCommandPair.getLeft(), invokableCommandPair.getRight());
                });

        Stream<CommandHandleBuilderImpl> stream2 = Arrays.stream(commandClass.getClasses())
                .filter(aClass -> aClass.isAnnotationPresent(Command.class))
                .map(aClass -> fromClass(commandClass, null, supplier, propertyMap));

        return Stream.concat(stream1, stream2).collect(Collectors.toList());
    }

    private CommandHandleBuilderImpl fromClass(Object commandObject, Class<?> commandClass, CommandObjectFactory factory, Supplier<Object> supplier, CommandPropertyMapImpl defaultPropertyMap) throws BreadBotException {
        CommandObjectFactory commandSupplier = createCommandFactory(commandClass, factory, supplier);

        final CommandPropertyMapImpl propertyMap = createPropertyMap(commandClass, defaultPropertyMap);

        List<Pair<Method, CommandPropertyMapImpl>> methods = getCommandMethodStream(commandClass)
                .map(method -> Pair.of(method, new CommandPropertyMapImpl(propertyMap, method.getAnnotations())))
                .collect(Collectors.toList());

        final Optional<Pair<Method, CommandPropertyMapImpl>> first = methods.stream()
                .filter(pair -> pair.getRight().testProperty(Command.class, command -> command.value().length == 1 && command.value()[0].length() == 0))
                .findFirst();

        final CommandParameterBuilder[] parameterBuilders;
        final InvokableCommand commandFunction;
        final CommandPropertyMapImpl methodPropertyMap;
        if (first.isPresent()) {
            final Pair<Method, CommandPropertyMapImpl> methodPair = first.get();
            methods.remove(methodPair);
            methodPropertyMap = methodPair.getRight();
            final Pair<CommandParameterBuilder[], InvokableCommand> biConsumerPair = mapMethod(methodPair.getLeft(), methodPair.getRight());
            parameterBuilders = biConsumerPair.getLeft();
            commandFunction = biConsumerPair.getRight();
        } else {
            parameterBuilders = null;
            commandFunction = null;
            methodPropertyMap = propertyMap;
        }
        final CommandHandleBuilderImpl commandHandleBuilder = new CommandHandleBuilderImpl(
                commandObject,
                commandClass,
                first.isPresent() ? first.get().getLeft() : null,
                clientBuilder,
                commandSupplier,
                parameterBuilders,
                commandFunction,
                methodPropertyMap);

        List<Class<?>> classes = Arrays.stream(commandClass.getClasses())
                .filter(aClass -> aClass.isAnnotationPresent(Command.class))
                .collect(Collectors.toList());

        setDefaultValues(commandClass, commandHandleBuilder);

        for (Pair<Method, CommandPropertyMapImpl> method : methods) {
            CommandHandleBuilder handle = fromMethod(method.getLeft(), method.getRight());
            commandHandleBuilder.addSubCommand(handle);
        }

        for (Class<?> aClass : classes) {
            CommandHandleBuilder subCommandBuilder;
            if (supplier == null)
                subCommandBuilder = fromClass(aClass, null, null, propertyMap);
            else {
                subCommandBuilder = fromClass(aClass, getSupplierForObject(aClass, supplier, aClass), supplier, propertyMap);
            }
            commandHandleBuilder.addSubCommand(subCommandBuilder);
        }
        return commandHandleBuilder;
    }

    private void setDefaultValues(Class<?> commandClass, CommandHandleBuilderImpl commandHandleBuilder) {
        String simpleName = commandClass.getSimpleName().toLowerCase();
        commandHandleBuilder.setName(simpleName);
        if (simpleName.endsWith("command") && simpleName.length() > 7) {
            simpleName = simpleName.substring(0, simpleName.length() - 7);
        }
        commandHandleBuilder.setKeys(simpleName);
        String[] packageNames = commandClass.getPackage().getName().split("\\.");
        String packageName = packageNames[packageNames.length - 1];
        if (packageName.matches("(command|cmd)(s)?") && packageNames.length > 1) {
            packageName = packageNames[packageNames.length - 2];
        }
        commandHandleBuilder.setGroup(packageName);
    }

    public CommandHandleBuilderInternal createHandleFromClass(Object obj, Class<?> commandClass, CommandPropertyMapImpl map) {

    }

    public CommandHandleBuilderInternal createHandleFromMethod(Object obj, Class<?> commandClass, CommandObjectFactory objectFactory, Method method, CommandPropertyMapImpl map) {
        final CommandParameterBuilderFactory parameterFactory = new CommandParameterBuilderFactory(map, method.getName());
        final Parameter[] parameters = method.getParameters();
        final CommandParameterBuilder[] parameterBuilders = new CommandParameterBuilder[parameters.length];
        Arrays.setAll(parameterBuilders, value -> parameterFactory.builder(parameters[value]));
        final MethodHandle handle;
        try {
            handle = MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new BreadBotException(method + " could not be accessed.", e);
        }
        final MethodHandle spread = handle.asSpreader(Object[].class, parameterBuilders.length);
        InvokableCommandHandle invokableCommandHandle = new InvokableCommandHandle(spread);
        CommandObjectFactory factory;
        if (objectFactory != null) {
            factory = objectFactory;
        } else {
            factory = getSupplierForClass(method.getDeclaringClass());
        }
        return new CommandHandleBuilderImpl(obj, commandClass, method, clientBuilder, factory, parameterBuilders, invokableCommandHandle, map);
    }

    @NotNull
    private Stream<Method> getCommandMethodStream(Class<?> commandClass) {
        return Arrays.stream(commandClass.getMethods())
                .filter(method -> method.getParameterCount() > 0)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.isAnnotationPresent(Command.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()));
    }

    @NotNull
    private CommandPropertyMapImpl createPropertyMap(Class<?> commandClass, CommandPropertyMapImpl defaultPropertyMap) {
        CommandPropertyMapImpl propertyMap;
        CommandPropertyMapImpl propertyMap1 = new CommandPropertyMapImpl();
        if (defaultPropertyMap != null) {
            propertyMap1.setDefaultProperties(defaultPropertyMap);
        } else {
            propertyMap1.setDefaultProperties(CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()));
        }
        propertyMap1.putAnnotations(commandClass.getAnnotations());
        propertyMap = propertyMap1;
        return propertyMap;
    }

    @NotNull
    private CommandObjectFactory createCommandFactory(Class<?> commandClass, CommandObjectFactory factory, Supplier<Object> supplier) {
        CommandObjectFactory commandSupplier;
        if (factory != null) {
            commandSupplier = factory;
        } else if (supplier != null) {
            commandSupplier = new CommandObjectFactory(ExceptionalSupplier.convert(supplier));
        } else {
            commandSupplier = getSupplierForClass(commandClass);
        }
        return commandSupplier;
    }

    private CommandObjectFactory getSupplierForClass(Class<?> klass) throws BreadBotException {
        ArrayDeque<MethodHandle> constructors = new ArrayDeque<>();
        Class<?> aClass = klass;
        while (aClass != null) {
            final Class<?> outClass = Modifier.isStatic(klass.getModifiers()) ? null : klass.getDeclaringClass();
            if (outClass == null) {
                try {
                    MethodHandle constructor = MethodHandles.publicLookup().findConstructor(aClass, MethodType.methodType(void.class));
                    constructors.addFirst(constructor);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new BreadBotException(aClass + " is registered as a command but does not have a public no-args constructor", e);
                }
                break;
            } else {
                try {
                    MethodHandle constructor = MethodHandles.publicLookup().findConstructor(aClass, MethodType.methodType(void.class, outClass));
                    constructors.addFirst(constructor);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new BreadBotException(aClass + " is registered as a command but does not have a public no-args constructor", e);
                }
            }
            aClass = outClass;
        }

        MethodHandle[] methodHandles = constructors.toArray(new MethodHandle[0]);

        if (methodHandles.length == 1) {
            MethodHandle methodHandle = methodHandles[0];
            return new CommandObjectFactory(methodHandle::invoke);
        } else {
            return new CommandObjectFactory(() -> {
                Object o = methodHandles[0].invoke();
                for (int i = 1; i < methodHandles.length; i++) {
                    o = methodHandles[i].invoke(o);
                }
                return o;
            });
        }
    }

    private CommandObjectFactory getSupplierForObject(Class<?> oClass, Supplier<Object> supplier, Class<?> klass) throws BreadBotException {
        ArrayDeque<MethodHandle> constructors = new ArrayDeque<>();
        Class<?> aClass = klass;
        while (aClass != oClass) {
            final Class<?> outClass = Modifier.isStatic(klass.getModifiers()) ? null : klass.getDeclaringClass();
            if (outClass == null) {
                throw new BreadBotException("SupplierForObject ClassMisMatch");
            } else {
                try {
                    MethodHandle constructor = MethodHandles.publicLookup().findConstructor(aClass, MethodType.methodType(void.class, outClass));
                    constructors.addFirst(constructor);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new BreadBotException(aClass + " is registered as a command but does not have a public no-args constructor", e);
                }
            }
            aClass = outClass;
        }

        MethodHandle[] methodHandles = constructors.toArray(new MethodHandle[0]);

        if (methodHandles.length == 1) {
            MethodHandle methodHandle = methodHandles[0];
            return new CommandObjectFactory(() -> methodHandle.invoke(supplier.get()));
        } else {
            return new CommandObjectFactory(() -> {
                Object o = methodHandles[0].invoke(supplier.get());
                for (int i = 1; i < methodHandles.length; i++) {
                    o = methodHandles[i].invoke(o);
                }
                return o;
            });
        }
    }
}
