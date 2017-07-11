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
package com.github.breadmoirai.samurai7.core.impl;

import com.github.breadmoirai.samurai7.core.CommandEngine;
import com.github.breadmoirai.samurai7.core.CommandEvent;
import com.github.breadmoirai.samurai7.core.IModule;
import com.github.breadmoirai.samurai7.core.command.ICommand;
import com.github.breadmoirai.samurai7.core.info.HelpCommand;
import com.github.breadmoirai.samurai7.core.response.Response;
import com.github.breadmoirai.samurai7.core.response.simple.EditResponse;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CommandEngineImpl implements CommandEngine {

    private final Map<Type, IModule> moduleTypeMap;
    private final Map<String, Class<? extends ICommand>> commandMap;

    private final Predicate<ICommand> postProcessPredicate;

    public CommandEngineImpl(List<IModule> modules, Map<String, Class<? extends ICommand>> commandMap, Predicate<ICommand> postProcessPredicate) {
        this.commandMap = commandMap;
        this.postProcessPredicate = postProcessPredicate == null ? iCommand -> true : postProcessPredicate;

        final HashMap<Type, IModule> typeMap = new HashMap<>(modules.size());
        for (IModule module : modules) {
            Class<?> moduleClass = module.getClass();
            do {
                typeMap.put(moduleClass, module);
                for (Class<?> inter : moduleClass.getInterfaces()) {
                    final List<Class<?>> interfaceList = getInterfaceHeirarchy(inter, IModule.class);
                    if (interfaceList != null) {
                        for (Class<?> interfaceClass : interfaceList)
                            typeMap.put(interfaceClass, module);
                    }
                }
            } while (IModule.class.isAssignableFrom(moduleClass = moduleClass.getSuperclass()));
        }
        this.moduleTypeMap = Collections.unmodifiableMap(typeMap);
        HelpCommand.initialize(modules, commandMap);
    }

    private List<Class<?>> getInterfaceHeirarchy(Class<?> from, Class<?> toSuper) {
        if (!from.isInterface())
            return null;
        if (from == toSuper)
            return new ArrayList<>();
        final Class<?>[] interfaces = from.getInterfaces();
        if (interfaces.length == 0)
            return null;
        final List<Class<?>> interfaceList = getInterfaceHeirarchy(interfaces[0], toSuper);
        if (interfaceList != null)
            interfaceList.add(0, from);
        return interfaceList;
    }


    @Override
    public Optional<Response> execute(CommandEvent event) {
        ICommand command;
        final String key = event.getKey().toLowerCase();
        if (key.equals("help")) {
            command = HelpCommand.newInstance();
        } else
            command = getCommand(key);
        if (command != null) {
            command.setEvent(event);
            if (command.setModules(moduleTypeMap)
                    && postProcessPredicate.test(command)) {
                final Optional<Response> call = command.call();
                call.ifPresent(r -> {
                    final CommandEvent evt = command.getEvent();
                    if (r.getAuthorId() == 0)
                        r.setAuthorId(evt.getAuthorId());
                    if (r.getChannelId() == 0)
                        r.setChannelId(evt.getChannelId());
                    if (r.getGuildId() == 0)
                        r.setGuildId(evt.getGuildId());
                    if (r.getMessageId() == 0)
                        r.setMessageId(evt.getMessageId());
                });
            }
        }
        return Optional.empty();
    }

    @Override
    public Class<? extends ICommand> getCommandClass(String key) {
        return null;
    }

    @Override
    public Stream<Class<? extends ICommand>> getCommands() {
        return null;
    }

    @Override
    public boolean hasCommand(String key) {
        return false;
    }
}
