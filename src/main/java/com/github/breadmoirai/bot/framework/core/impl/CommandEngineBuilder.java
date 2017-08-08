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
package com.github.breadmoirai.bot.framework.core.impl;

import com.github.breadmoirai.bot.framework.core.CommandEngine;
import com.github.breadmoirai.bot.framework.core.CommandEvent;
import com.github.breadmoirai.bot.framework.core.IModule;
import com.github.breadmoirai.bot.framework.core.command.*;
import com.github.breadmoirai.bot.framework.error.EmptyCommandAnnotation;
import com.github.breadmoirai.bot.framework.error.MissingCommandAnnotation;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.reflections.Reflections;

import java.util.*;
import java.util.function.Predicate;

public class CommandEngineBuilder {

    private static final SimpleLog LOG = SimpleLog.getLog("CommandBuilder");
    private final List<IModule> modules;

    private Predicate<Message> preProcessPredicate;
    private Predicate<CommandEvent> postProcessPredicate;

    private Map<String, CommandWrapper> commandMap = new HashMap<>();

    public CommandEngineBuilder(List<IModule> modules) {
        this.modules = modules;
    }

    public CommandEngineBuilder addPreProcessPredicate(Predicate<Message> predicate) {
        if (preProcessPredicate == null) {
            preProcessPredicate = predicate;
        } else preProcessPredicate = preProcessPredicate.and(predicate);
        return this;
    }

    public CommandEngineBuilder addPostProcessPredicate(Predicate<CommandEvent> predicate) {
        if (postProcessPredicate == null) {
            postProcessPredicate = predicate;
        } else postProcessPredicate = postProcessPredicate.and(predicate);
        return this;
    }


    private CommandEngineBuilder registerCommand(CommandWrapper wrapper, String... keys) {
        final Class<?> commandClass = wrapper.getCommandClass();
        if (keys.length == 0) {
            LOG.warn("No key found for " + commandClass.getSimpleName());
            return this;
        }
        for (String key : keys) {
            final String key1 = key.toLowerCase();
            if (commandMap.containsKey(key1)) {
                final CommandWrapper existing = commandMap.get(key1);
                LOG.warn("Key \"" + key + "\" for Command " + commandClass.getSimpleName() + " is already mapped to Command " + existing.getCommandClass().getSimpleName());
            } else {
                commandMap.put(key, wrapper);
                LOG.info("\"" + key + "\" mapped to " + commandClass.getSimpleName());
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public CommandEngineBuilder registerCommand(Class<?> commandClass) {
        final String[] keys;
        if (MultiCommand.class.isAssignableFrom(commandClass)) {
            keys = MultiCommand.register((Class<? extends MultiCommand>) commandClass);
        } else if (MultiSubCommand.class.isAssignableFrom(commandClass)) {
            keys = MultiSubCommand.register((Class<? extends MultiSubCommand>) commandClass);
        } else if (ModuleMultiCommand.class.isAssignableFrom(commandClass)) {
            keys = ModuleMultiCommand.register((Class<? extends ModuleMultiCommand>) commandClass);
        } else if (ModuleMultiSubCommand.class.isAssignableFrom(commandClass)) {
            keys = ModuleMultiSubCommand.register((Class<? extends ModuleMultiSubCommand>) commandClass);
        } else if (BiModuleMultiCommand.class.isAssignableFrom(commandClass)) {
            keys = BiModuleMultiCommand.register((Class<? extends BiModuleMultiCommand>) commandClass);
        } else if (BiModuleMultiSubCommand.class.isAssignableFrom(commandClass)) {
            keys = BiModuleMultiSubCommand.register((Class<? extends BiModuleMultiSubCommand>) commandClass);
        } else if (ICommand.class.isAssignableFrom(commandClass)) {
            CommandEngineBuilder.checkCommandAnnotation(commandClass);
            final Command annotation = commandClass.getAnnotation(Command.class);
            keys = annotation.value();
        } else {

        }
        if (keys == null)
            LOG.warn("No key found for " + commandClass.getSimpleName());
        else
            registerCommand(commandClass, keys);
        return this;
    }

    public CommandEngineBuilder registerCommand(String commandPackagePrefix) {
        final Reflections reflections = new Reflections(commandPackagePrefix);
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Command.class);
        for (Class<?> commandClass : classes) {
            if (ICommand.class.isAssignableFrom(commandClass)) {
                //noinspection unchecked
                registerCommand((Class<? extends ICommand>) commandClass);
            }
        }
        final Set<Class<? extends ModuleMultiCommand>> multiCommands = reflections.getSubTypesOf(ModuleMultiCommand.class);
        for (Class<? extends ModuleMultiCommand> commandClass : multiCommands) registerCommand(commandClass);
        return this;
    }

    Map<String, Class<? extends ICommand>> getCommandMap() {
        return commandMap;
    }

    Predicate<Message> getPreProcessPredicate() {
        return preProcessPredicate;
    }

    Predicate<ICommand> getPostProcessPredicate() {
        return postProcessPredicate;
    }


    public boolean hasModule(Class<? extends IModule> moduleClass) {
        return moduleClass != null && modules.stream().map(Object::getClass).anyMatch(moduleClass::isAssignableFrom);
    }

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleClass The class of the Module to find
     * @return The module if found. Else {@code null}.
     */
    public <T extends IModule> T getModule(Class<T> moduleClass) {
        //noinspection unchecked
        return moduleClass == null ? null : modules.stream().filter(module -> moduleClass.isAssignableFrom(module.getClass())).map(iModule -> (T) iModule).findAny().orElse(null);
    }

    public void addModule(IModule module) {
        checkDuplicateModules(module);
        modules.add(module);
    }

    private void checkDuplicateModules(IModule module) {
        Class<? extends IModule> moduleClass = module.getClass();
        while (Arrays.stream(moduleClass.getInterfaces()).noneMatch(i -> i == IModule.class)) {
            //noinspection unchecked
            moduleClass = (Class<? extends IModule>) moduleClass.getSuperclass();
        }
        if (hasModule(moduleClass))
            LOG.warn("Duplicate Module: There are two or more modules of type " + moduleClass.toString());
    }

    CommandEngine build() {
        return new CommandEngineImpl(modules, commandMap, postProcessPredicate);
    }

    public static void checkCommandAnnotation(Class<?> klass) {
        final Command annotation = klass.getAnnotation(Command.class);
        if (annotation == null) {
            throw new MissingCommandAnnotation(klass);
        }
        final String[] value = annotation.value();
        if (value.length == 0) {
            throw new EmptyCommandAnnotation(klass);
                    }
        Checks.noneContainBlanks(Arrays.asList(value), "Command Class: " + klass.getName() + " @Command keys");

    }

}
