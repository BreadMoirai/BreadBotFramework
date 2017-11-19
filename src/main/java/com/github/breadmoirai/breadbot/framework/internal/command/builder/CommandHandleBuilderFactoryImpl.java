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
package com.github.breadmoirai.breadbot.framework.internal.command.builder;

import com.github.breadmoirai.breadbot.framework.*;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;
import com.github.breadmoirai.breadbot.framework.error.CommandInitializationException;
import com.github.breadmoirai.breadbot.framework.error.MissingMainCommandException;
import com.github.breadmoirai.breadbot.framework.error.TooManyMainCommandsException;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandObjectFactory;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandPackageProperties;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.internal.command.InvokableCommandHandle;
import com.github.breadmoirai.breadbot.framework.internal.parameter.CommandParameterFunctionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandleBuilderFactoryImpl implements CommandHandleBuilderFactoryInternal {

    private static final Logger log = LoggerFactory.getLogger("CommandBuilder");

    private final BreadBotClientBuilder clientBuilder;

    public CommandHandleBuilderFactoryImpl(BreadBotClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Consumer<CommandEvent> onCommand) {
        return new CommandHandleBuilderImpl(
                onCommand,
                onCommand.getClass(),
                null,
                clientBuilder,
                new CommandObjectFactory(() -> onCommand),
                new CommandParameterBuilder[]{new CommandParameterBuilderSpecificImpl(null, "This parameter of type CommandEvent is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent()))},
                (o, objects) -> {
                    onCommand.accept(((CommandEvent) objects[0]));
                    return null;
                },
                new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(onCommand.getClass().getPackage())));
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Class<?> commandClass) {
        final Method method = getMainMethod(commandClass, true);
        final CommandObjectFactory factory = getSupplierForClass(commandClass);
        CommandPropertyMapImpl map = new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()), commandClass.getAnnotations());
        return createCommandHandleBuilderInternal(null, commandClass, method, factory, null, null, map);
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Object commandObject) {
        final Class<?> aClass = commandObject.getClass();
        final Method method = getMainMethod(aClass, true);
        final CommandObjectFactory factory = new CommandObjectFactory(() -> commandObject);
        CommandPropertyMapImpl map = new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(aClass.getPackage()), aClass.getAnnotations());
        return createCommandHandleBuilderInternal(commandObject, aClass, method, factory, null, null, map);
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Supplier<?> commandSupplier, Object o) {
        final Class<?> aClass = o.getClass();
        final Method method = getMainMethod(aClass, true);
        final CommandObjectFactory factory = new CommandObjectFactory(commandSupplier::get);
        CommandPropertyMapImpl map = new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(aClass.getPackage()), aClass.getAnnotations());
        return createCommandHandleBuilderInternal(null, aClass, method, factory, aClass, commandSupplier, map);
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommands(String packageName) {
        final Reflections reflections = new Reflections(packageName);
        final Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        List<CommandHandleBuilderInternal> builders = new ArrayList<>();
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

            Stream<Method> methods = Arrays.stream(commandClass.getMethods());
            Stream<Method> innerMethods = Arrays.stream(commandClass.getClasses()).map(Class::getMethods).flatMap(Arrays::stream);
            Stream<Method> methodStream = Stream.concat(methods, innerMethods);
            boolean hasCommandAnnotation = methodStream.anyMatch(method -> method.isAnnotationPresent(Command.class) || method.isAnnotationPresent(MainCommand.class));
            if (!hasCommandAnnotation) continue;
            if (Arrays.stream(commandClass.getMethods()).anyMatch(method -> method.isAnnotationPresent(MainCommand.class))) {
                builders.add(createCommand(commandClass));
            } else {
                builders.addAll(createCommands(commandClass));
            }
        }
        return builders;
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommands(Class<?> commandClass) {
        return getSubCommands(
                null,
                commandClass,
                getSupplierForClass(commandClass),
                new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()), commandClass.getAnnotations()),
                null,
                null);
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommands(Object commandObject) {
        Class<?> commandClass = commandObject.getClass();
        return getSubCommands(
                commandObject,
                commandClass,
                new CommandObjectFactory(() -> commandObject),
                new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()), commandClass.getAnnotations()),
                null,
                null);
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommands(Supplier<?> commandSupplier, Object commandObject) {
        Class<?> commandClass = commandObject.getClass();
        return getSubCommands(
                null,
                commandClass,
                new CommandObjectFactory(commandSupplier::get),
                new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()), commandClass.getAnnotations()),
                commandClass,
                commandSupplier);
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        return commandClasses.stream()
                .flatMap(commandClass -> {
                    if (Arrays.stream(commandClass.getMethods()).anyMatch(method -> method.isAnnotationPresent(MainCommand.class))) {
                        return Stream.of(createCommand(commandClass));
                    } else {
                        return createCommands(commandClass).stream();
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommandsFromObjects(Collection<?> commandObjects) {
        return commandObjects.stream()
                .flatMap(commandObject -> {
                    if (Arrays.stream(commandObject.getClass().getMethods()).anyMatch(method -> method.isAnnotationPresent(MainCommand.class))) {
                        return Stream.of(createCommand(commandObject));
                    } else {
                        return createCommands(commandObject).stream();
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers) {
        return commandSuppliers.stream()
                .flatMap(commandSupplier -> {
                    final Object commandObject = commandSupplier.get();
                    final Class<?> commandClass = commandObject.getClass();
                    List<Method> mainMethods = Arrays.stream(commandClass.getMethods())
                            .filter(method -> method.isAnnotationPresent(MainCommand.class))
                            .collect(Collectors.toList());
                    CommandPropertyMapImpl classPropertyMap = new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()), commandClass.getAnnotations());
                    if (mainMethods.isEmpty()) {
                        return getSubCommands(
                                null,
                                commandClass,
                                new CommandObjectFactory(commandSupplier::get),
                                classPropertyMap,
                                commandClass,
                                commandSupplier).stream();
                    } else if (mainMethods.size() > 1) {
                        throw new TooManyMainCommandsException(commandClass);
                    }
                    final CommandObjectFactory factory = new CommandObjectFactory(commandSupplier::get);
                    return Stream.of(createCommandHandleBuilderInternal(
                            null,
                            commandClass,
                            mainMethods.get(0),
                            factory,
                            commandClass,
                            commandSupplier,
                            classPropertyMap));
                }).collect(Collectors.toList());
    }

    private CommandHandleBuilderInternal createCommandHandleBuilderInternal(Object commandObject, Class<?> aClass, Method method, CommandObjectFactory factory, Class<?> supplierReturnType, Supplier<?> supplier, CommandPropertyMap defaultMap) {
        final CommandHandleBuilderInternal builder;
        CommandPropertyMapImpl methodPorp = new CommandPropertyMapImpl(defaultMap, method.getAnnotations());
        builder = createHandleFromMethod(
                commandObject,
                aClass,
                method,
                factory,
                methodPorp);
        clientBuilder.applyModifiers(builder);
        getSubCommands(commandObject, aClass, factory, defaultMap, supplierReturnType, supplier).forEach(builder::putCommandHandle);
        return builder;
    }

    private List<CommandHandleBuilderInternal> getSubCommands(@Nullable Object commandObject,
                                                              @NotNull Class<?> commandClass,
                                                              @NotNull CommandObjectFactory factory,
                                                              @Nullable CommandPropertyMap propertyMap,
                                                              @Nullable Class<?> supplierReturnType,
                                                              @Nullable Supplier<?> supplier) {
        ArrayList<CommandHandleBuilderInternal> builders = new ArrayList<>();
        for (Method method : commandClass.getMethods()) {
            if (!method.isAnnotationPresent(Command.class)) continue;
            CommandPropertyMapImpl map = new CommandPropertyMapImpl(propertyMap, method.getAnnotations());
            String[] keys = map.getDeclaredProperty(Command.class).value();
            CommandHandleBuilderInternal handle = createHandleFromMethod(commandObject, commandClass, method, factory, map);
            clientBuilder.applyModifiers(handle);
            builders.add(handle);
        }

        for (Class<?> inner : commandClass.getClasses()) {
            final Method method = getMainMethod(inner, false);
            if (method == null) continue;
            CommandPropertyMapImpl map = new CommandPropertyMapImpl(propertyMap, inner.getAnnotations());
            final CommandObjectFactory innerFactory = supplier != null ? getSupplierForObject(supplierReturnType, supplier, inner) : getSupplierForClass(inner);
            CommandHandleBuilderInternal handle = createCommandHandleBuilderInternal(commandObject, inner, method, innerFactory, supplierReturnType, supplier, map);
            builders.add(handle);
        }
        return builders;
    }

    private Method getMainMethod(Class<?> commandClass, boolean mustBePresent) {
        List<Method> mainMethods = Arrays.stream(commandClass.getMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.isAnnotationPresent(MainCommand.class))
                .collect(Collectors.toList());
        if (mainMethods.isEmpty()) {
            if (!mustBePresent) return null;
            throw new MissingMainCommandException(commandClass);
        } else if (mainMethods.size() > 1) {
            throw new TooManyMainCommandsException(commandClass);
        }
        return mainMethods.get(0);
    }

    public CommandHandleBuilderInternal createHandleFromMethod(Object obj, Class<?> commandClass, Method method, CommandObjectFactory objectFactory, CommandPropertyMapImpl map) {
        final CommandParameterBuilderFactory parameterFactory = new CommandParameterBuilderFactory(clientBuilder, map, method.getName());
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

    private CommandObjectFactory getSupplierForClass(Class<?> klass) throws BreadBotException {
        ArrayDeque<MethodHandle> constructors = new ArrayDeque<>();
        Class<?> aClass = klass;
        while (aClass != null) {
            final Class<?> outClass;
            if (Modifier.isStatic(klass.getModifiers())) outClass = null;
            else outClass = klass.getDeclaringClass();
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
            //noinspection Duplicates
            return new CommandObjectFactory(() -> {
                Object o = methodHandles[0].invoke();
                for (int i = 1; i < methodHandles.length; i++) {
                    o = methodHandles[i].invoke(o);
                }
                return o;
            });
        }
    }

    private CommandObjectFactory getSupplierForObject(Class<?> oClass, Supplier<?> supplier, Class<?> uClass) throws BreadBotException {
        ArrayDeque<MethodHandle> constructors = new ArrayDeque<>();
        Class<?> aClass = uClass;
        boolean isStatic = false;
        while (aClass != oClass) {
            final Class<?> outClass;
            if (Modifier.isStatic(uClass.getModifiers())) {
                isStatic = true;
                try {
                    MethodHandle constructor = MethodHandles.publicLookup().findConstructor(aClass, MethodType.methodType(void.class));
                    constructors.addFirst(constructor);
                    break;
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new BreadBotException(aClass + " is registered as a command but does not have a public no-args constructor", e);
                }
            } else outClass = uClass.getDeclaringClass();
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
            if (!isStatic)
                return new CommandObjectFactory(() -> methodHandle.invoke(supplier.get()));
            else
                return new CommandObjectFactory(methodHandle::invoke);
        } else {
            if (!isStatic)
                return new CommandObjectFactory(() -> {
                    Object o = methodHandles[0].invoke(supplier.get());
                    for (int i = 1; i < methodHandles.length; i++) {
                        o = methodHandles[i].invoke(o);
                    }
                    return o;
                });
            else {
                //noinspection Duplicates
                return new CommandObjectFactory(() -> {
                    Object o = methodHandles[0].invoke();
                    for (int i = 1; i < methodHandles.length; i++) {
                        o = methodHandles[i].invoke(o);
                    }
                    return o;
                });
            }
        }
    }

    public void requireNoCommandAnnotation(Class<?> aClass) {
        Command command = aClass.getAnnotation(Command.class);
        if (command != null) throw new CommandInitializationException(
                aClass + " is registered as multiple commands but is marked as a single command. " +
                        "If you want to register " + aClass.getSimpleName() + " as multiple commands, " +
                        "you must remove the @Command annotation. " +
                        "Otherwise use #createCommand or #addCommand instead to register as a single command with subcommands.");
    }
}
