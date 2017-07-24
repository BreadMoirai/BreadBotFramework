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
package net.breadmoirai.sbf.core.impl;

import net.breadmoirai.sbf.core.CommandEngine;
import net.breadmoirai.sbf.core.IModule;
import net.breadmoirai.sbf.core.command.*;
import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

public class CommandEngineBuilder {

    private static Logger logger = LoggerFactory.getLogger(CommandEngine.class);
    private final List<IModule> modules;

    private Predicate<Message> preProcessPredicate;
    private Predicate<ICommand> postProcessPredicate;

    private Map<String, Class<? extends ICommand>> commandMap = new HashMap<>();

    public CommandEngineBuilder(List<IModule> modules) {
        this.modules = modules;
    }

    public CommandEngineBuilder addPreProcessPredicate(Predicate<Message> predicate) {
        if (preProcessPredicate == null) {
            preProcessPredicate = predicate;
        } else preProcessPredicate = preProcessPredicate.and(predicate);
        return this;
    }

    public CommandEngineBuilder addPostProcessPredicate(Predicate<ICommand> predicate) {
        if (postProcessPredicate == null) {
            postProcessPredicate = predicate;
        } else postProcessPredicate = postProcessPredicate.and(predicate);
        return this;
    }


    private CommandEngineBuilder registerCommand(Class<? extends ICommand> commandClass, String... keys) {
        for (String key : keys) {
            if (key == null || keys.length == 0) {
                logger.error("No key found for " + commandClass.getSimpleName());
                continue;
            }
            final String key1 = key.toLowerCase();
            if (commandMap.containsKey(key1)) {
                final Class<? extends ICommand> existing = commandMap.get(key1);
                logger.error("Key \"" + key + "\" for Command " + commandClass.getSimpleName() + " is already mapped to Command " + existing.getSimpleName());
            } else {
                commandMap.put(key, commandClass);
                logger.info("\"" + key + "\" mapped to " + commandClass.getSimpleName());
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public CommandEngineBuilder registerCommand(Class<? extends ICommand> commandClass) {
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
        } else if (commandClass.isAnnotationPresent(Key.class)) {
            keys = commandClass.getAnnotation(Key.class).value();
        } else {
            keys = null;
        }
        if (keys == null)
            logger.error("No key found for " + commandClass.getSimpleName());
        else
            registerCommand(commandClass, keys);
        return this;
    }

    public CommandEngineBuilder registerCommand(String commandPackagePrefix) {
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(commandPackagePrefix)))
//                .setUrls(ClasspathHelper.forPackage(commandPackagePrefix))
//                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));
        final Reflections reflections = new Reflections(commandPackagePrefix);
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Key.class);
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
            logger.warn("Duplicate Module: There are two or more modules of type " + moduleClass.toString());
    }

    CommandEngine build() {
        return new CommandEngineImpl(modules, commandMap, postProcessPredicate);
    }

}
