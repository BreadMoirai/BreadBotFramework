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
package com.github.breadmoirai.framework.core.impl;

import com.github.breadmoirai.framework.core.*;
import com.github.breadmoirai.framework.event.CommandEvent;
import com.github.breadmoirai.framework.event.ICommandEventFactory;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SamuraiClientImpl implements SamuraiClient {

    private JDA jda;

    private final IEventManager eventManager;
    private final ICommandEventFactory eventFactory;
    private final CommandEngine commandEngine;
    private final List<IModule> modules;
    private final Map<Type, IModule> moduleTypeMap;

    public SamuraiClientImpl(List<IModule> modules, IEventManager eventManager, ICommandEventFactory eventFactory, CommandEngineBuilder engineBuilder) {
        this.modules = modules;
        modules.forEach(module -> module.init(engineBuilder, this));
        eventManager.register(this.new SamuraiEventListener(engineBuilder.getPreProcessPredicate()));

        if (eventManager instanceof InterfacedEventManager)
            modules.stream().filter(net.dv8tion.jda.core.hooks.EventListener.class::isInstance).forEach(eventManager::register);
        else
            modules.forEach(eventManager::register);
        this.eventManager = eventManager;
        this.eventFactory = eventFactory;
        this.commandEngine = engineBuilder.build();

        final HashMap<Type, IModule> typeMap = new HashMap<>(modules.size());
        for (IModule module : modules) {
            Class<?> moduleClass = module.getClass();
            do {
                typeMap.put(moduleClass, module);
                for (Class<?> inter : moduleClass.getInterfaces()) {
                    final List<Class<?>> interfaceList = getInterfaceHierarchy(inter, IModule.class);
                    if (interfaceList != null) {
                        for (Class<?> interfaceClass : interfaceList)
                            typeMap.put(interfaceClass, module);
                    }
                }
            } while (IModule.class.isAssignableFrom(moduleClass = moduleClass.getSuperclass()));
        }
        this.moduleTypeMap = typeMap;
    }

    private List<Class<?>> getInterfaceHierarchy(Class<?> from, Class<?> toSuper) {
        if (!from.isInterface())
            return null;
        if (from == toSuper)
            return new ArrayList<>();
        final Class<?>[] interfaces = from.getInterfaces();
        if (interfaces.length == 0)
            return null;
        final List<Class<?>> interfaceList = getInterfaceHierarchy(interfaces[0], toSuper);
        if (interfaceList != null)
            interfaceList.add(0, from);
        return interfaceList;
    }

    @Override
    public JDA getJDA() {
        return jda;
    }

    @Override
    public boolean hasModule(String moduleName) {
        return moduleName != null && modules.stream().map(IModule::getName).anyMatch(moduleName::equalsIgnoreCase);
    }

    @Override
    public boolean hasModule(Class<? extends IModule> moduleClass) {
        return moduleTypeMap.containsKey(moduleClass);
    }

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleClass The class of the Module to find
     * @return Optional containing the module if found.
     */
    @Override
    public <T extends IModule> T getModule(Class<T> moduleClass) {
        //noinspection unchecked
        return (T) moduleTypeMap.get(moduleClass);
    }

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleName the name of the module to find. If the module does not override {@link com.github.breadmoirai.framework.core.IModule#getName IModule#getName} the name of the class is used.
     * @return Optional containing the module if foundd.
     */
    @Override
    public IModule getModule(String moduleName) {
        return moduleName == null ? null : modules.stream().filter(module -> module.getName().equalsIgnoreCase(moduleName)).findAny().orElse(null);
    }

    @Override
    public IModule getModule(Type moduleType) {
        return moduleTypeMap.get(moduleType);
    }

    @Override
    public CommandEngine getCommandEngine() {
        return commandEngine;
    }

    private class SamuraiEventListener extends ListenerAdapter {

        private final Predicate<Message> preProcessPredicate;

        SamuraiEventListener(Predicate<Message> preProcessPredicate) {
            this.preProcessPredicate = preProcessPredicate == null ? message -> true : preProcessPredicate;
        }

        @SubscribeEvent
        @Override
        public void onReady(ReadyEvent event) {
            SamuraiClientImpl.this.jda = event.getJDA();
        }

        @SubscribeEvent
        @Override
        public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
            onGuildMessageEvent(event, event.getMessage());
        }

        @SubscribeEvent
        @Override
        public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
            onGuildMessageEvent(event, event.getMessage());
        }

        private void onGuildMessageEvent(GenericGuildMessageEvent event, Message message) {
            if (preProcessPredicate.test(message)) {
                final CommandEvent commandEvent = eventFactory.createEvent(event, message, SamuraiClientImpl.this);
                if (commandEvent != null) {
                    commandEngine.handle(commandEvent);
                    eventManager.handle(commandEvent);
                }
            }
        }
    }
}
