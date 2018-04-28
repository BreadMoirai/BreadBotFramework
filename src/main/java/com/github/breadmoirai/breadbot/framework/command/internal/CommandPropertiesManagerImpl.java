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

package com.github.breadmoirai.breadbot.framework.command.internal;

import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommands;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandPropertiesManager;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.internal.builder.CommandHandleBuilderInternal;
import com.github.breadmoirai.breadbot.framework.defaults.DefaultCommandProperties;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandPropertiesManagerImpl implements CommandPropertiesManager<CommandPropertiesManagerImpl> {

    private static Map<Package, CommandPropertyMapImpl> packageMap = new HashMap<>();
    private final Map<Class<?>, BiConsumer<?, CommandHandleBuilder>> commandPropertyMap = new HashMap<>();
    private final Map<Class<?>, BiConsumer<?, CommandParameterBuilder>> parameterPropertyMap = new HashMap<>();
    private List<String> preprocessorPriorityList = Collections.emptyList();

    public CommandPropertiesManagerImpl() {
        new DefaultCommandProperties().initialize(this);
    }

    private static CommandPropertyMapImpl createPropertiesForPackage(Package p) {
        final String name = p.getName();
        final int i = name.lastIndexOf('.');
        final CommandPropertyMapImpl map;
        if (i != -1) {
            final String parentPackageName = name.substring(0, i);
            final Package aPackage = Package.getPackage(parentPackageName);
            if (aPackage != null) {
                map = new CommandPropertyMapImpl(getPP(aPackage), p.getAnnotations());
            } else {
                map = new CommandPropertyMapImpl(null, p.getAnnotations());
            }
        } else {
            map = new CommandPropertyMapImpl(null, p.getAnnotations());
        }
        return map;
    }

    public static CommandPropertyMapImpl getPP(Package p) {
        if (p == null) return null;
        return packageMap.computeIfAbsent(p, CommandPropertiesManagerImpl::createPropertiesForPackage);
    }

    @Override
    public CommandPropertiesManagerImpl clearCommandModifiers(Class<?> propertyType) {
        commandPropertyMap.remove(propertyType);
        return this;
    }

    @Override
    public <T> CommandPropertiesManagerImpl bindCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        commandPropertyMap.merge(propertyType, configurator, (c1, c2) -> {
            @SuppressWarnings("unchecked") final BiConsumer<T, CommandHandleBuilder> cc1 = (BiConsumer<T, CommandHandleBuilder>) c1;
            @SuppressWarnings("unchecked") final BiConsumer<T, CommandHandleBuilder> cc2 = (BiConsumer<T, CommandHandleBuilder>) c2;
            return cc1.andThen(cc2);
        });
        return this;
    }

    @Override
    public CommandPropertiesManagerImpl clearParameterModifiers(Class<?> parameterType) {
        parameterPropertyMap.remove(parameterType);
        return this;
    }

    @Override
    public <T> CommandPropertiesManagerImpl bindParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        parameterPropertyMap.merge(propertyType, configurator, (c1, c2) -> {
            @SuppressWarnings("unchecked") final BiConsumer<T, CommandParameterBuilder> cc1 = (BiConsumer<T, CommandParameterBuilder>) c1;
            @SuppressWarnings("unchecked") final BiConsumer<T, CommandParameterBuilder> cc2 = (BiConsumer<T, CommandParameterBuilder>) c2;
            return cc1.andThen(cc2);
        });
        return this;
    }

    @Override
    public void applyModifiers(CommandHandleBuilder builder) {
        for (Class<?> aClass : commandPropertyMap.keySet()) {
            if (aClass != null) if (builder.hasProperty(aClass))
                applyCommandModifier(aClass, builder);
        }
        applyCommandModifier(null, builder);
        applyConfigureCommand(builder);
    }

    @Override
    public void applyModifiers(CommandParameterBuilder builder) {
        for (Class<?> aClass : parameterPropertyMap.keySet()) {
            if (aClass != null && builder.hasProperty(aClass))
                applyParameterModifier(aClass, builder);
        }
        applyParameterModifier(null, builder);
    }

    @Override
    public <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        BiConsumer<?, CommandHandleBuilder> biConsumer = commandPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandHandleBuilder> consumer = (BiConsumer<T, CommandHandleBuilder>) biConsumer;
        return consumer;
    }

    @Override
    public <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        BiConsumer<T, CommandHandleBuilder> commandModifier = getCommandModifier(propertyType);
        if (commandModifier != null) {
            T property = builder.getProperty(propertyType);
            commandModifier.accept(property, builder);
        }
    }

    private void applyConfigureCommand(CommandHandleBuilder builder) {
        Class<?> declaringClass = builder.getDeclaringClass();
        if (Consumer.class.isAssignableFrom(declaringClass))
            return;

        for (Method method : builder.getDeclaringClass().getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers))
                continue;
            if (method.getParameterCount() != 1)
                continue;
            if (method.getParameters()[0].getType() != CommandHandleBuilder.class)
                continue;
            if (!method.isAnnotationPresent(ConfigureCommands.class) && !method.isAnnotationPresent(ConfigureCommand.class))
                continue;
            final Object o;
            Object declaringObject = builder.getDeclaringObject();
            if (declaringObject != null && !(declaringObject instanceof Consumer)) {
                o = declaringObject;
            } else if (Modifier.isStatic(modifiers)) {
                o = ((CommandHandleBuilderInternal) builder).getObjectFactory().getOrNull();
            } else {
                o = null;
            }
            ConfigureCommands annotation = method.getAnnotation(ConfigureCommands.class);
            ConfigureCommand[] value;
            if (annotation != null) {
                value = annotation.value();
            } else {
                value = new ConfigureCommand[]{method.getAnnotation(ConfigureCommand.class)};
            }
            for (ConfigureCommand configureCommand : value) {
                if (configureCommand.value().equals(builder.getName()) || configureCommand.value().isEmpty()) {
                    try {
                        method.invoke(o, builder);
                        break;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        String msg = String.format("An Error occurred when attempting to configure CommandHandleBuilder[%s] with method %s#%s",
                                                   builder.getName(), method.getDeclaringClass().getName(), method.getName());
                        throw new BreadBotException(msg, e);
                    }
                }
            }
        }
    }

    @Override
    public <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        BiConsumer<?, CommandParameterBuilder> biConsumer = parameterPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked")
        BiConsumer<T, CommandParameterBuilder> consumer = (BiConsumer<T, CommandParameterBuilder>) biConsumer;
        return consumer;
    }

    @Override
    public <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        BiConsumer<T, CommandParameterBuilder> commandModifier = getParameterModifier(propertyType);
        if (commandModifier != null) {
            T property = builder.getProperty(propertyType);
            commandModifier.accept(property, builder);
        }
    }

    private <T> void associatePreprocessor(Class<T> propertyType, Function<T, CommandPreprocessor> factory) {
        bindCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(factory.apply(t)));
    }

    @Override
    public List<String> getPreprocessorPriorityList() {
        return preprocessorPriorityList;
    }

    @Override
    public CommandPropertiesManagerImpl setPreprocessorPriority(String... identifiers) {
        preprocessorPriorityList = Arrays.asList(identifiers);
        return this;
    }

    @Override
    public CommandPropertiesManagerImpl setPreprocessorPriority(List<String> identifierList) {
        preprocessorPriorityList = identifierList;
        return this;
    }

    private int getPriority(String identifier, List<String> list) {
        final int i = list.indexOf(identifier);
        if (i != -1) return i;
        final int j = list.indexOf(null);
        if (j != -1) return j;
        else return list.size();
    }

    @Override
    public Comparator<CommandPreprocessor> getPriorityComparator() {
        return new PriorityComparator(preprocessorPriorityList);
    }

    public Comparator<CommandPreprocessor> getPreprocessorComparator(String... identifier) {
        return new PriorityComparator(Arrays.asList(identifier));
    }

    @Override
    public CommandPropertiesManagerImpl self() {
        return this;
    }

    public class PriorityComparator implements Comparator<CommandPreprocessor> {

        private final List<String> identifierList;

        public PriorityComparator(List<String> identifierList) {
            this.identifierList = identifierList;
        }

        @Override
        public int compare(CommandPreprocessor o1, CommandPreprocessor o2) {
            return getPriority(o1.getIdentifier(), identifierList) - getPriority(o2.getIdentifier(), identifierList);
        }
    }


}