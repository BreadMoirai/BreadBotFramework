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
import com.github.breadmoirai.breadbot.framework.command.DefaultCommand;
import com.github.breadmoirai.breadbot.framework.command.impl.CommandObjectFactory;
import com.github.breadmoirai.breadbot.framework.command.impl.CommandPackageProperties;
import com.github.breadmoirai.breadbot.framework.command.impl.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.command.impl.InvokableCommandHandle;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParameterFunctionImpl;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.util.ExceptionalSupplier;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CommandHandleBuilderFactoryImpl implements CommandHandleBuilderFactoryInternal {

    private final BreadBotClientBuilder clientBuilder;

    public CommandHandleBuilderFactoryImpl(BreadBotClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public CommandHandleBuilderInternal createCommandHandle(Consumer<CommandEvent> onCommand) {
        return new CommandHandleBuilderImpl(
                onCommand,
                null,
                null,
                clientBuilder,
                new CommandObjectFactory(() -> onCommand),
                new CommandParameterBuilder[]{new CommandParameterBuilderSpecificImpl(null, "This parameter of type CommandEvent is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent()))},
                (o, objects) -> onCommand.accept(((CommandEvent) objects[0])),
                new CommandPropertyMapImpl(CommandPackageProperties.getPropertiesForPackage(onCommand.getClass().getPackage())));
    }

    @Override
    public CommandHandleBuilderInternal createCommandHandle(Supplier<Object> commandSupplier) {
        final Object o = commandSupplier.get();
        final Class<?> aClass = o.getClass();
        final Method method = findDefaultMethod(aClass);
        final CommandObjectFactory factory = new CommandObjectFactory(ExceptionalSupplier.convert(commandSupplier));
        return createCommandHandleBuilderInternal(null, aClass, method, factory, aClass, commandSupplier);
    }

    @Override
    public CommandHandleBuilderInternal createCommandHandle(Class<?> commandClass) {
        final Method method = findDefaultMethod(commandClass);
        final CommandObjectFactory factory = getSupplierForClass(commandClass);
        return createCommandHandleBuilderInternal(null, commandClass, method, factory, null, null);
    }

    @Override
    public CommandHandleBuilderInternal createCommandHandle(Object commandObject) {
        final Class<?> aClass = commandObject.getClass();
        final Method method = findDefaultMethod(aClass);
        final CommandObjectFactory factory = new CommandObjectFactory(() -> commandObject);
        return createCommandHandleBuilderInternal(commandObject, aClass, method, factory, null, null);
    }

    private CommandHandleBuilderInternal createCommandHandleBuilderInternal(Object commandObject, Class<?> aClass, Method method, CommandObjectFactory factory, Class<?> supplierReturnType, Supplier<?> supplier) {
        final CommandHandleBuilderInternal builder;
        final CommandPropertyMap pkg = CommandPackageProperties.getPropertiesForPackage(aClass.getPackage());
        final CommandPropertyMapImpl prop = new CommandPropertyMapImpl(pkg, aClass.getAnnotations());
        if (method != null) {
            CommandPropertyMapImpl methodPorp = new CommandPropertyMapImpl(prop, method.getAnnotations());
            builder = createHandleFromMethod(
                    commandObject,
                    aClass,
                    method,
                    factory,
                    methodPorp);
        } else {
            builder = createHandleFromClass(
                    commandObject,
                    aClass,
                    factory,
                    prop);
        }
        clientBuilder.getCommandProperties().applyModifiers(builder);
        setDefaultValues(aClass, builder);
        addSubCommands(commandObject, aClass, factory, prop, builder, supplierReturnType, supplier);
        return builder;
    }

    private void addSubCommands(Object commandObject,
                                Class<?> commandClass,
                                CommandObjectFactory factory,
                                CommandPropertyMapImpl propertyMap,
                                CommandHandleBuilderInternal builder,
                                Class<?> supplierReturnType,
                                Supplier<?> supplier) {
        CommandPropertyMap pkg = CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage());
        for (Method method : commandClass.getMethods()) {
            CommandPropertyMapImpl map = new CommandPropertyMapImpl(propertyMap, method.getAnnotations());
            if (!map.hasDeclaredProperty(Command.class)) continue;
            String[] keys = map.getDeclaredProperty(Command.class).value();
            if (keys.length == 1 && keys[0].isEmpty()) continue;
            CommandHandleBuilderInternal handle = createHandleFromMethod(commandObject, commandClass, method, factory, map);
            setDefaultValues(method, handle);
            builder.putCommandHandle(handle);
        }

        for (Class<?> inner : commandClass.getClasses()) {
            CommandPropertyMapImpl map = new CommandPropertyMapImpl(propertyMap, inner.getAnnotations());
            if (!map.hasDeclaredProperty(Command.class)) continue;
            final Method method = findDefaultMethod(commandClass);
            final CommandObjectFactory innerFactory = supplier != null ? getSupplierForObject(supplierReturnType, supplier, inner) : getSupplierForClass(commandClass);
            CommandHandleBuilderInternal handle = createCommandHandleBuilderInternal(commandObject, commandClass, method, innerFactory, supplierReturnType, supplier);
            builder.putCommandHandle(handle);
        }
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommandHandles(Class<?> commandClass) {
        return null;
    }

    @Override
    public List<CommandHandleBuilderInternal> createCommandHandles(String packageName) {
        List<CommandHandleBuilderInternal> builders = new ArrayList<>();
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
            if (commandClass.isAnnotationPresent(Command.class)) {
                builders.add(createCommandHandle(commandClass));
            } else {
                builders.addAll(createCommandHandles(commandClass));
            }
        }
        return builders;
    }

    private Method findDefaultMethod(Class<?> commandClass) {
        return Arrays.stream(commandClass.getMethods())
                .filter(method -> {
                    if (method.isAnnotationPresent(DefaultCommand.class)) return true;
                    else if (method.isAnnotationPresent(Command.class)) {
                        Command annotation = method.getAnnotation(Command.class);
                        String[] value = annotation.value();
                        if (value.length == 1) {
                            return value[0].isEmpty();
                        }
                    }
                    return false;
                }).findAny().orElse(null);
    }

    private void setDefaultValues(Class<?> commandClass, CommandHandleBuilderInternal builder) {
        String simpleName = commandClass.getSimpleName().toLowerCase();
        if (simpleName.endsWith("command") && simpleName.length() > 7) {
            simpleName = simpleName.substring(0, simpleName.length() - 7);
        }
        if (builder.getName() == null)
            builder.setName(simpleName);
        if (builder.getKeys() == null)
            builder.setKeys(simpleName);
        String[] packageNames = commandClass.getPackage().getName().split("\\.");
        String packageName = packageNames[packageNames.length - 1];
        if (packageName.matches("(command|cmd)(s)?") && packageNames.length > 1) {
            packageName = packageNames[packageNames.length - 2];
        }
        if (builder.getGroup() == null)
            builder.setGroup(packageName);
    }

    private void setDefaultValues(Method method, CommandHandleBuilderInternal builder) {
        String simpleName = method.getName().toLowerCase();
        if (simpleName.endsWith("command") && simpleName.length() > 7) {
            simpleName = simpleName.substring(0, simpleName.length() - 7);
        }
        if (builder.getName() == null)
            builder.setName(simpleName);
        if (builder.getKeys() == null)
            builder.setKeys(simpleName);
        String[] packageNames = method.getDeclaringClass().getPackage().getName().split("\\.");
        String packageName = packageNames[packageNames.length - 1];
        if (packageName.matches("(command|cmd)(s)?") && packageNames.length > 1) {
            packageName = packageNames[packageNames.length - 2];
        }
        if (builder.getGroup() == null)
            builder.setGroup(packageName);
    }

    public CommandHandleBuilderInternal createHandleFromClass(Object obj, Class<?> commandClass, CommandObjectFactory objectFactory, CommandPropertyMapImpl map) {
        CommandObjectFactory factory;
        if (objectFactory != null) {
            factory = objectFactory;
        } else {
            factory = getSupplierForClass(commandClass);
        }
        return new CommandHandleBuilderImpl(obj, commandClass, null, clientBuilder, factory, null, null, map);
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

    private CommandObjectFactory getSupplierForObject(Class<?> oClass, Supplier<?> supplier, Class<?> uClass) throws BreadBotException {
        ArrayDeque<MethodHandle> constructors = new ArrayDeque<>();
        Class<?> aClass = uClass;
        while (aClass != oClass) {
            final Class<?> outClass = Modifier.isStatic(uClass.getModifiers()) ? null : uClass.getDeclaringClass();
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
