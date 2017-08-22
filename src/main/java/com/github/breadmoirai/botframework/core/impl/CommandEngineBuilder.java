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
package com.github.breadmoirai.botframework.core.impl;

import com.github.breadmoirai.botframework.core.CommandEngine;
import com.github.breadmoirai.botframework.core.IModule;
import com.github.breadmoirai.botframework.core.command.*;
import com.github.breadmoirai.botframework.error.DuplicateCommandKeyException;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

public class CommandEngineBuilder {

    private static final SimpleLog LOG = SimpleLog.getLog("CommandBuilder");
    private final List<IModule> modules;

    private Predicate<Message> preProcessPredicate;

    private Map<String, ICommand> commandMap = new HashMap<>();

    public CommandEngineBuilder(List<IModule> modules) {
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

    /**
     * This method ignores any annotations attached to the class and registers it with the provided keys.
     * This is useful for short and simple commands such as
     * <pre><code>
     *     registerCommand(event -> {
     *          if (event.getAuthorId() == 12356789L)
     *              event.getJDA().shutdown();
     *     },"shutdown");
     * </code></pre>
     * <p>
     * <p>Commands registered this way will use the provided
     * {@link com.github.breadmoirai.botframework.core.command.ICommand} object
     * to perform it's action whenever it is called.
     *
     * @param command an object ICommand
     * @param keys    keys which trigger the command
     * @return this
     * @see com.github.breadmoirai.botframework.core.impl.CommandEngineBuilder#registerCommand(java.lang.Class)
     * @see com.github.breadmoirai.botframework.core.impl.CommandEngineBuilder#registerCommand(java.lang.Object)
     */
    public CommandEngineBuilder registerCommand(ICommand command, String... keys) {
        final Class<?> commandClass = command.getClass();
        if (keys.length == 0) {
            LOG.fatal("No key found for " + commandClass.getSimpleName());
            return this;
        }

        for (String key : keys) {
            final String key1 = key.toLowerCase();
            if (commandMap.containsKey(key1)) {
                final ICommand existing = commandMap.get(key1);
                LOG.fatal(String.format("Key \"%s\" for %s is already mapped to %s", key, command, existing));
            } else {
                commandMap.put(key, command);
                LOG.info(String.format("\"%s\" mapped to %s", key, command));
            }
        }
        return this;
    }

    public CommandEngineBuilder registerCommand(Object command) {
        if (command instanceof Class) {
            return registerCommand(((Class) command));
        } else if (command instanceof String) {
            return registerCommand(((String) command));
        } else if (command instanceof ICommand) {
            final Class<?> commandClass = command.getClass();
            final Command key = commandClass.getAnnotation(Command.class);
            if (key == null || key.value().length == 0) {
                String name = commandClass.getSimpleName().toLowerCase();
                if (!name.startsWith("command") && name.endsWith("command")) {
                    name = name.replace("command", "");
                }
                registerCommand(((ICommand) command), name);
            } else {
                registerCommand(((ICommand) command), key.value());
            }
        } else {
            try {
                final CommandAdapter commandAdapter = new CommandAdapter(command);
                registerCommand(commandAdapter, commandAdapter.getKeys());
            } catch (NoSuchMethodException | IllegalAccessException | DuplicateCommandKeyException e) {
                LOG.fatal(e);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public CommandEngineBuilder registerCommand(Class<?> commandClass) {
        final CommandWrapper wrapper;
        try {
            if (BiModuleCommand.class.isAssignableFrom(commandClass)) {
                wrapper = new BiModuleCommandWrapper((Class<? extends BiModuleCommand>) commandClass);
            } else if (ModuleCommand.class.isAssignableFrom(commandClass)) {
                wrapper = new ModuleCommandWrapper((Class<? extends ModuleCommand>) commandClass);
            } else if (ICommand.class.isAssignableFrom(commandClass)) {
                wrapper = new RawCommandWrapper((Class<? extends ICommand>) commandClass);
            } else {
                wrapper = new CommandAdapter(commandClass);
            }
        } catch (NoSuchMethodException | DuplicateCommandKeyException | IllegalAccessException e) {
            LOG.fatal(e);
            return this;
        }
        registerCommand(wrapper, wrapper.getKeys());
        return this;
    }

    public CommandEngineBuilder registerCommand(String commandPackagePrefix) {
        final Reflections reflections = new Reflections(commandPackagePrefix);
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
            if (ICommand.class.isAssignableFrom(commandClass)) {
                registerCommand(commandClass);
            } else {
                if (commandClass.isAnnotationPresent(Command.class)
                        || Arrays.stream(commandClass.getMethods())
                        .anyMatch(method -> method.isAnnotationPresent(Command.class))) {
                    registerCommand(commandClass);
                }
            }
        }
        return this;
    }

    Map<String, ICommand> getCommandMap() {
        return commandMap;
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
        return new CommandEngineImpl(modules, commandMap);

    }

}
