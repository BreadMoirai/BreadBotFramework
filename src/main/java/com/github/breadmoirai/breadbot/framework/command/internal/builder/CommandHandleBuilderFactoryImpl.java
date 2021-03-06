/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.breadmoirai.breadbot.framework.command.internal.builder;

import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandObjectFactory;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertiesManagerImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.InvokableCommandHandle;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;
import com.github.breadmoirai.breadbot.framework.error.CommandInitializationException;
import com.github.breadmoirai.breadbot.framework.error.MissingMainCommandException;
import com.github.breadmoirai.breadbot.framework.error.TooManyMainCommandsException;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.inject.BreadInjector;
import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CommandParameterBuilderImpl;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandleBuilderFactoryImpl implements CommandHandleBuilderFactoryInternal {

    private static final Logger log = LoggerFactory.getLogger("CommandBuilder");

    private final BreadBotBuilder clientBuilder;

    public CommandHandleBuilderFactoryImpl(BreadBotBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Consumer<CommandEvent> onCommand) {
        CommandParameterBuilder[] parameterBuilders = new CommandParameterBuilder[1];
        CommandHandleBuilderImpl commandHandleBuilder = new CommandHandleBuilderImpl(
                onCommand,
                onCommand.getClass(),
                null,
                clientBuilder,
                CommandObjectFactory.empty(),
                parameterBuilders,
                (o, objects) -> {
                    onCommand.accept(((CommandEvent) objects[0]));
                    return null;
                },
                new CommandPropertyMapImpl(CommandPropertiesManagerImpl.getPP(onCommand.getClass().getPackage())));
        parameterBuilders[0] = new CommandParameterBuilderImpl(clientBuilder, commandHandleBuilder, null, null);
        parameterBuilders[0].setParser((parameter, list, parser) -> parser.getEvent());
        return commandHandleBuilder;
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Class<?> commandClass) {
        final Method method = getMainMethod(commandClass, true);
        final CommandObjectFactory factory = getSupplierForClass(commandClass);
        CommandPropertyMapImpl map = new CommandPropertyMapImpl(
                CommandPropertiesManagerImpl.getPP(commandClass.getPackage()), commandClass.getAnnotations());
        return createCommandHandleBuilderInternal(null, commandClass, method, factory, null, null, map);
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Object commandObject) {
        final Class<?> aClass = commandObject.getClass();
        final Method method = getMainMethod(aClass, true);
        final CommandObjectFactory factory = CommandObjectFactory.of(aClass, commandObject);
        CommandPropertyMapImpl map = new CommandPropertyMapImpl(CommandPropertiesManagerImpl.getPP(aClass.getPackage()),
                                                                aClass.getAnnotations());
        return createCommandHandleBuilderInternal(commandObject, aClass, method, factory, null, null, map);
    }

    @Override
    public CommandHandleBuilderInternal createCommand(Supplier<?> commandSupplier, Object o) {
        final Class<?> aClass = o.getClass();
        final Method method = getMainMethod(aClass, true);
        final CommandObjectFactory factory = CommandObjectFactory.of(aClass, commandSupplier::get);
        CommandPropertyMapImpl map = new CommandPropertyMapImpl(CommandPropertiesManagerImpl.getPP(aClass.getPackage()),
                                                                aClass.getAnnotations());
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
            Stream<Method> innerMethods = Arrays.stream(commandClass.getClasses())
                    .map(Class::getMethods)
                    .flatMap(Arrays::stream);
            Stream<Method> methodStream = Stream.concat(methods, innerMethods);
            boolean hasCommandAnnotation = methodStream.anyMatch(
                    method -> method.isAnnotationPresent(Command.class) || method.isAnnotationPresent(
                            MainCommand.class));
            if (!hasCommandAnnotation) continue;
            if (Arrays.stream(commandClass.getMethods())
                    .anyMatch(method -> method.isAnnotationPresent(MainCommand.class))) {
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
                new CommandPropertyMapImpl(CommandPropertiesManagerImpl.getPP(commandClass.getPackage()),
                                           commandClass.getAnnotations()),
                null,
                null);
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommands(Object commandObject) {
        Class<?> commandClass = commandObject.getClass();
        return getSubCommands(
                commandObject,
                commandClass,
                CommandObjectFactory.of(commandClass, commandObject),
                new CommandPropertyMapImpl(CommandPropertiesManagerImpl.getPP(commandClass.getPackage()),
                                           commandClass.getAnnotations()),
                null,
                null);
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommands(Supplier<?> commandSupplier, Object commandObject) {
        Class<?> commandClass = commandObject.getClass();
        return getSubCommands(
                null,
                commandClass,
                CommandObjectFactory.of(commandClass, commandSupplier),
                new CommandPropertyMapImpl(CommandPropertiesManagerImpl.getPP(commandClass.getPackage()),
                                           commandClass.getAnnotations()),
                commandClass,
                commandSupplier);
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        return commandClasses.stream()
                .flatMap(commandClass -> {
                    if (Arrays.stream(commandClass.getMethods())
                            .anyMatch(method -> method.isAnnotationPresent(MainCommand.class))) {
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
                    if (Arrays.stream(commandObject.getClass().getMethods())
                            .anyMatch(method -> method.isAnnotationPresent(MainCommand.class))) {
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
                    CommandPropertyMapImpl classPropertyMap = new CommandPropertyMapImpl(
                            CommandPropertiesManagerImpl.getPP(commandClass.getPackage()),
                            commandClass.getAnnotations());
                    if (mainMethods.isEmpty()) {
                        return getSubCommands(
                                null,
                                commandClass,
                                CommandObjectFactory.of(commandClass, commandSupplier),
                                classPropertyMap,
                                commandClass,
                                commandSupplier).stream();
                    } else if (mainMethods.size() > 1) {
                        throw new TooManyMainCommandsException(commandClass);
                    }
                    final CommandObjectFactory factory = CommandObjectFactory.of(commandClass, commandSupplier);
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

    private CommandHandleBuilderInternal createCommandHandleBuilderInternal(Object commandObject, Class<?> aClass,
                                                                            Method method, CommandObjectFactory factory,
                                                                            Class<?> supplierReturnType,
                                                                            Supplier<?> supplier,
                                                                            CommandPropertyMapImpl defaultMap) {
        final CommandHandleBuilderInternal builder;
        CommandPropertyMapImpl methodPorp = new CommandPropertyMapImpl(defaultMap, method.getAnnotations());
        builder = createHandleFromMethod(
                commandObject,
                aClass,
                method,
                factory,
                methodPorp);
        clientBuilder.applyModifiers(builder);
        getSubCommands(commandObject, aClass, factory, defaultMap, supplierReturnType, supplier).forEach(
                builder::putCommandHandle);
        return builder;
    }

    private List<CommandHandleBuilderInternal> getSubCommands(Object commandObject,
                                                              Class<?> commandClass,
                                                              CommandObjectFactory factory,
                                                              CommandPropertyMapImpl propertyMap,
                                                              Class<?> supplierReturnType,
                                                              Supplier<?> supplier) {
        ArrayList<CommandHandleBuilderInternal> builders = new ArrayList<>();
        for (Method method : commandClass.getMethods()) {
            if (!method.isAnnotationPresent(Command.class)) continue;
            CommandPropertyMapImpl map = new CommandPropertyMapImpl(propertyMap, method.getAnnotations());
            String[] keys = map.getProperty(Command.class).value();
            CommandHandleBuilderInternal handle = createHandleFromMethod(commandObject, commandClass, method, factory,
                                                                         map);
            clientBuilder.applyModifiers(handle);
            builders.add(handle);
        }

        for (Class<?> inner : commandClass.getClasses()) {
            final Method method = getMainMethod(inner, false);
            if (method == null) continue;
            CommandPropertyMapImpl map = new CommandPropertyMapImpl(propertyMap, inner.getAnnotations());
            final CommandObjectFactory innerFactory = supplier != null ? getSupplierForObject(supplierReturnType,
                                                                                              supplier,
                                                                                              inner) :
                    getSupplierForClass(
                            inner);
            CommandHandleBuilderInternal handle = createCommandHandleBuilderInternal(commandObject, inner, method,
                                                                                     innerFactory, supplierReturnType,
                                                                                     supplier, map);
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

    public CommandHandleBuilderInternal createHandleFromMethod(Object obj, Class<?> commandClass, Method method,
                                                               CommandObjectFactory objectFactory,
                                                               CommandPropertyMapImpl map) {

        final Parameter[] parameters = method.getParameters();
        final CommandParameterBuilder[] parameterBuilders = new CommandParameterBuilder[parameters.length];

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

        CommandHandleBuilderImpl commandHandleBuilder = new CommandHandleBuilderImpl(obj, commandClass, method,
                                                                                     clientBuilder, factory,
                                                                                     parameterBuilders,
                                                                                     invokableCommandHandle, map);

        Arrays.setAll(parameterBuilders,
                      value -> new CommandParameterBuilderImpl(clientBuilder, commandHandleBuilder, parameters[value],
                                                               map));
        return commandHandleBuilder;
    }

    private CommandObjectFactory getSupplierForClass(Class<?> klass) throws BreadBotException {
        ArrayDeque<MethodHandle> constructors = new ArrayDeque<>();
        ArrayDeque<Class<?>> classes = new ArrayDeque<>();
        Class<?> aClass = klass;
        while (aClass != null) {
            final Class<?> outClass;
            if (Modifier.isStatic(aClass.getModifiers()))
                outClass = null;
            else outClass = klass.getDeclaringClass();
            if (outClass == null) {
                try {
                    MethodHandle constructor = MethodHandles.publicLookup()
                            .findConstructor(aClass, MethodType.methodType(void.class));
                    constructors.addFirst(constructor);
                    classes.addFirst(aClass);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new BreadBotException(
                            aClass + " is registered as a command but does not have a public no-args constructor", e);
                }
                break;
            } else {
                AddFirstInnerConstructor(constructors, classes, aClass, outClass);
            }
            aClass = outClass;
        }

        MethodHandle[] methodHandles = constructors.toArray(new MethodHandle[0]);
        Class<?>[] classArray = classes.toArray(new Class<?>[0]);

        if (methodHandles.length == 1) {
            MethodHandle methodHandle = methodHandles[0];
            return CommandObjectFactory.of(classArray[0], methodHandle);
        } else {
            return getCommandObjectFactoryForNestedConstructors(methodHandles, classArray);
        }
    }

    private CommandObjectFactory getSupplierForObject(Class<?> supplierReturnType, Supplier<?> supplier,
                                                      Class<?> uClass) throws
                                                                       BreadBotException {
        ArrayDeque<MethodHandle> constructors = new ArrayDeque<>();
        ArrayDeque<Class<?>> classes = new ArrayDeque<>();
        Class<?> aClass = uClass;
        boolean isStatic = Modifier.isStatic(uClass.getModifiers());
        while (aClass != supplierReturnType) {
            final Class<?> outClass;
            if (Modifier.isStatic(aClass.getModifiers())) {
                try {
                    MethodHandle constructor = MethodHandles.publicLookup()
                            .findConstructor(aClass, MethodType.methodType(void.class));
                    constructors.addFirst(constructor);
                    classes.addFirst(aClass);
                    break;
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new BreadBotException(
                            aClass + " is registered as a command but does not have a public no-args constructor", e);
                }
            } else outClass = uClass.getDeclaringClass();
            if (outClass == null) {
                throw new BreadBotException("SupplierForObject ClassMisMatch");
            } else {
                AddFirstInnerConstructor(constructors, classes, aClass, outClass);
            }
            aClass = outClass;
        }

        MethodHandle[] methodHandles = constructors.toArray(new MethodHandle[0]);
        Class<?>[] classArray = classes.toArray(new Class<?>[0]);

        if (methodHandles.length == 1) {
            MethodHandle methodHandle = methodHandles[0];
            final Class<?> aClass1 = classArray[0];
            if (!isStatic) {
                return new CommandObjectFactory(null) {
                    private final Class<?> supplierType = supplierReturnType, handleType = aClass1;
                    private final Supplier<?> supp = supplier;
                    private final MethodHandle handle = methodHandle;
                    private BreadInjector.Injector suppInj, handleInj;

                    @Override
                    public void setInjector(BreadInjector injector) {
                        suppInj = injector.getInjectorFor(supplierType);
                        handleInj = injector.getInjectorFor(handleType);
                    }

                    @Override
                    public Object get() throws Throwable {
                        Object o = supp.get();
                        if (suppInj != null) {
                            suppInj.inject(o);
                        }
                        o = handle.invoke(o);
                        if (handleInj != null) {
                            handleInj.inject(o);
                        }
                        return o;
                    }
                };
            } else
                return CommandObjectFactory.of(classArray[0], methodHandle);
        } else {
            if (!isStatic)
                return new CommandObjectFactory(null) {
                    private final Supplier<?> supp = supplier;
                    private final Class<?> suppClass = supplierReturnType;
                    private final Class<?>[] classes = classArray;
                    private final MethodHandle[] handles = methodHandles;
                    private BreadInjector.Injector suppInj = null;
                    private BreadInjector.Injector[] injectors = null;

                    @Override
                    public void setInjector(BreadInjector injector) {
                        suppInj = injector.getInjectorFor(suppClass);
                        injectors = new BreadInjector.Injector[classes.length];
                        for (int i = 0; i < classes.length; i++) {
                            injectors[i] = injector.getInjectorFor(classes[i]);
                        }
                    }

                    @Override
                    public Object get() throws Throwable {
                        if (injectors == null) {
                            Object o = supp.get();
                            for (final MethodHandle handle : handles) {
                                o = handle.invoke(o);
                            }
                            return o;
                        } else {
                            Object o = supp.get();
                            if (suppInj != null) {
                                suppInj.inject(o);
                            }
                            for (int i = 0; i < handles.length; i++) {
                                o = handles[i].invoke(o);
                                if (injectors[i] != null) {
                                    injectors[i].inject(o);
                                }
                            }
                            return o;
                        }
                    }

                };
            else {
                return getCommandObjectFactoryForNestedConstructors(methodHandles, classArray);
            }
        }
    }

    private CommandObjectFactory getCommandObjectFactoryForNestedConstructors(MethodHandle[] methodHandles,
                                                                              Class<?>[] classArray) {
        return new CommandObjectFactory(null) {
            private final Class<?>[] classes = classArray;
            private final MethodHandle[] handles = methodHandles;
            private BreadInjector.Injector[] injectors = null;

            @Override
            public void setInjector(BreadInjector injector) {
                injectors = new BreadInjector.Injector[classes.length];
                for (int i = 0; i < classes.length; i++) {
                    injectors[i] = injector.getInjectorFor(classes[i]);
                }
            }

            @Override
            public Object get() throws Throwable {
                if (injectors == null) {
                    Object o = handles[0].invoke();
                    for (int i = 1; i < handles.length; i++) {
                        o = handles[i].invoke(o);
                    }
                    return o;
                } else {
                    Object o = handles[0].invoke();
                    if (injectors[0] != null) {
                        injectors[0].inject(o);
                    }
                    for (int i = 1; i < handles.length; i++) {
                        o = handles[i].invoke(o);
                        if (injectors[i] != null) {
                            injectors[i].inject(o);
                        }
                    }
                    return o;
                }
            }

        };
    }

    private void AddFirstInnerConstructor(ArrayDeque<MethodHandle> constructors, ArrayDeque<Class<?>> classes,
                                          Class<?> aClass, Class<?> outClass) {
        try {
            MethodHandle constructor = MethodHandles.publicLookup()
                    .findConstructor(aClass, MethodType.methodType(void.class, outClass));
            constructors.addFirst(constructor);
            classes.addFirst(aClass);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new BreadBotException(
                    aClass + " is registered as a command but does not have a public no-args constructor", e);
        }
    }

    public void requireNoCommandAnnotation(Class<?> aClass) {
        Command command = aClass.getAnnotation(Command.class);
        if (command != null) throw new CommandInitializationException(
                aClass + " is registered as multiple commands but is marked as a single command. " +
                        "If you want to register " + aClass.getSimpleName() + " as multiple commands, " +
                        "you must remove the @Command annotation. " +
                        "Otherwise use #createCommand or #addCommand instead to register as a single command with " +
                        "subcommands.");
    }
}