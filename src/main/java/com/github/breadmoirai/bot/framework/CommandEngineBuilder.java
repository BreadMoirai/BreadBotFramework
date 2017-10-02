/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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
package com.github.breadmoirai.bot.framework;

import com.github.breadmoirai.bot.framework.command.Command;
import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.builder.CommandBuilder;
import com.github.breadmoirai.bot.framework.command.builder.CommandHandleBuilder;
import com.github.breadmoirai.bot.framework.command.builder.FunctionalCommandBuilder;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import com.github.breadmoirai.bot.framework.impl.CommandEngineImpl;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CommandEngineBuilder {

    private static final SimpleLog LOG = SimpleLog.getLog("CommandBuilder");
    private final List<ICommandModule> modules;

    private Predicate<Message> preProcessPredicate;

    private List<CommandHandleBuilder> commandBuilderList = new ArrayList<>();

    public CommandEngineBuilder(List<ICommandModule> modules) {
        this.modules = modules;
    }

    public CommandEngineBuilder addPreProcessPredicate(Predicate<Message> predicate) {
        if (preProcessPredicate == null) {
            preProcessPredicate = predicate;
        } else preProcessPredicate = preProcessPredicate.and(predicate);
        return this;
    }

    public Predicate<Message> getPreProcessPredicate() {
        return preProcessPredicate;
    }

    private void addCommandBuilder(CommandHandleBuilder builder) {
        commandBuilderList.add(builder);
        LOG.info("Registered Command \"" + builder.getName() + "\" with " + Arrays.toString(builder.getKeys()) + ".");
    }

    public CommandEngineBuilder registerCommand(String name, Consumer<CommandEvent> commandFunction, String... keys) {
        addCommandBuilder(new FunctionalCommandBuilder(name, commandFunction).setKeys(keys));
        return this;
    }

    public CommandEngineBuilder registerCommand(Consumer<CommandEvent> commandFunction, Consumer<FunctionalCommandBuilder> configurator) {
        final FunctionalCommandBuilder fcb = new FunctionalCommandBuilder(null, commandFunction);
        configurator.accept(fcb);
        addCommandBuilder(fcb);
        return this;
    }

    public CommandEngineBuilder registerCommand(Object command) {
        addCommandBuilder(new CommandBuilder(command));
        return this;
    }

    public CommandEngineBuilder registerCommand(Object command, Consumer<CommandBuilder> configurator) {
        final CommandBuilder builder = new CommandBuilder(command);
        configurator.accept(builder);
        addCommandBuilder(builder);
        return this;
    }


    public CommandEngineBuilder registerCommand(Class<?> commandClass) {
        final CommandBuilder commandBuilder = new CommandBuilder(commandClass);
        addCommandBuilder(commandBuilder);
        return this;
    }

    public CommandEngineBuilder registerCommand(Class<?> commandClass, Consumer<CommandBuilder> configurator) {

        final CommandBuilder commandBuilder = new CommandBuilder(commandClass);
        configurator.accept(commandBuilder);
        addCommandBuilder(commandBuilder);
        return this;
    }

    public CommandEngineBuilder registerCommand(String packageName, Consumer<CommandBuilder> configurator) {
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
            final CommandBuilder commandBuilder = new CommandBuilder(commandClass);
            configurator.accept(commandBuilder);
            addCommandBuilder(commandBuilder);
        }
        return this;
    }

    public boolean hasModule(Class<? extends ICommandModule> moduleClass) {
        return moduleClass != null && modules.stream().map(Object::getClass).anyMatch(moduleClass::isAssignableFrom);
    }

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleClass The class of the Module to find
     * @return The module if found. Else {@code null}.
     */
    public <T extends ICommandModule> T getModule(Class<T> moduleClass) {
        //noinspection unchecked
        return moduleClass == null ? null : modules.stream().filter(module -> moduleClass.isAssignableFrom(module.getClass())).map(iModule -> (T) iModule).findAny().orElse(null);
    }

    public void addModule(ICommandModule module) {
        checkDuplicateModules(module);
        modules.add(module);
    }

    private void checkDuplicateModules(ICommandModule module) {
        Class<? extends ICommandModule> moduleClass = module.getClass();
        while (Arrays.stream(moduleClass.getInterfaces()).noneMatch(i -> i == ICommandModule.class)) {
            //noinspection unchecked
            moduleClass = (Class<? extends ICommandModule>) moduleClass.getSuperclass();
        }
        if (hasModule(moduleClass))
            LOG.warn("Duplicate Module: There are two or more modules of type " + moduleClass.toString());
    }

    public CommandEngine build() {
        final HashMap<String, CommandHandle> commandMap = new HashMap<>();
        commandBuilderList.stream().map(CommandHandleBuilder::build).forEach(commandExecutor -> {
            final String[] keys = commandExecutor.getEffectiveKeys();
            if (keys == null || keys.length == 0) {
                throw new RuntimeException("No keys defined for " + commandExecutor.getName());
            }
            for (String key : keys) {
                commandMap.put(key, commandExecutor);
            }
        });

        return new CommandEngineImpl(modules, commandMap);
    }

}
