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

package com.github.breadmoirai.breadbot.framework.internal;

import com.github.breadmoirai.breadbot.framework.BreadBot;
import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.command.AbstractCommand;
import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.CommandEngine;
import com.github.breadmoirai.breadbot.framework.command.CommandResultManager;
import com.github.breadmoirai.breadbot.framework.error.DuplicateCommandKeyException;
import com.github.breadmoirai.breadbot.framework.event.CommandEventFactory;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameterManager;
import com.github.breadmoirai.breadbot.util.EventStringIterator;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreadBotImpl implements BreadBot, EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(BreadBot.class);
    private final CommandResultManager resultManager;
    private final CommandParameterManager argumentTypes;
    private final CommandEventFactory eventFactory;
    private final CommandEngine commandEngine;
    private final List<CommandPlugin> modules;
    private final Map<Type, CommandPlugin> moduleTypeMap;
    private final Map<String, Command> commandMap;
    private final boolean shouldEvaluateCommandOnMessageUpdate;

    public BreadBotImpl(
            List<CommandPlugin> modules,
            List<Command> commands,
            CommandResultManager resultManager,
            CommandParameterManager argumentTypes,
            CommandEventFactory eventFactory,
            boolean shouldEvaluateCommandOnMessageUpdate) {
        this.modules = Collections.unmodifiableList(modules);
        this.resultManager = resultManager;
        this.argumentTypes = argumentTypes;
        this.eventFactory = eventFactory;
        this.shouldEvaluateCommandOnMessageUpdate = shouldEvaluateCommandOnMessageUpdate;

        HashMap<String, Command> handleMap = new HashMap<>();
        for (Command command : commands) {
            String[] keys = command.getKeys();
            for (String key : keys) {
                if (handleMap.containsKey(key)) {
                    throw new DuplicateCommandKeyException(key, command, handleMap.get(key));
                }
                handleMap.put(key, command);
            }
            LOG.info("Command Created: " + command);
        }
        this.commandMap = handleMap;

        final HashMap<Type, CommandPlugin> typeMap = new HashMap<>(modules.size());
        for (CommandPlugin module : modules) {
            Class<?> moduleClass = module.getClass();
            do {
                typeMap.put(moduleClass, module);
                for (Class<?> inter : moduleClass.getInterfaces()) {
                    final List<Class<?>> interfaceList = getInterfaceHierarchy(inter, CommandPlugin.class);
                    if (interfaceList != null) {
                        for (Class<?> interfaceClass : interfaceList)
                            typeMap.put(interfaceClass, module);
                    }
                }
            } while (CommandPlugin.class.isAssignableFrom(moduleClass = moduleClass.getSuperclass()));
        }

        this.moduleTypeMap = typeMap;

        this.commandEngine = getCommandEngine(commandMap);

        LOG.info("BreadBotClient Initialized");
    }

    private static CommandEngine getCommandEngine(Map<String, Command> commandMap) {
        return event -> {
            Command commandHandle = commandMap.get(event.getKeys()[0].toLowerCase());
            if (commandHandle != null) {
                if (event.isHelpEvent()) {
                    if (!commandHandle.handle(event, new EventStringIterator(event))) {
                        Command help = commandMap.get("help");
                        if (help != null) {
                            LOG.debug(String.format("Executing Command: %s (%s)", help.getName(), help.getGroup()));
                            help.handle(event, new EventStringIterator(event));
                        }
                    }
                } else {
                    LOG.debug(String.format("Executing Command: %s (%s)", commandHandle.getName(),
                                            commandHandle.getGroup()));
                    commandHandle.handle(event, new EventStringIterator(event));
                    if (commandHandle instanceof AbstractCommand) {
                        LOG.debug("Command Execution Complete");
                    }

                }
            } else if (event.isHelpEvent()) {
                Command help = commandMap.get("help");
                if (help != null) {
                    LOG.debug("Executing Command: help");
                    help.handle(event, new EventStringIterator(event));
                }
            }
        };
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
    public boolean hasPlugin(Class<? extends CommandPlugin> pluginClass) {
        return moduleTypeMap.containsKey(pluginClass);
    }

    @Override
    public <T extends CommandPlugin> T getPlugin(Class<T> pluginClass) {
        //noinspection unchecked
        return (T) moduleTypeMap.get(pluginClass);
    }

    public CommandPlugin getPlugin(Type pluginType) {
        return moduleTypeMap.get(pluginType);
    }

    @Override
    public List<CommandPlugin> getPlugins() {
        return modules;
    }

    @Override
    public CommandEngine getCommandEngine() {
        return commandEngine;
    }

    @Override
    public CommandParameterManager getArgumentTypes() {
        return argumentTypes;
    }

    @Override
    public CommandResultManager getResultManager() {
        return resultManager;
    }

    @Override
    public Map<String, Command> getCommandMap() {
        return commandMap;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildMessageReceivedEvent) {
            onGuildMessageReceived(((GuildMessageReceivedEvent) event));
        } else if (event instanceof GuildMessageUpdateEvent) {
            onGuildMessageUpdate(((GuildMessageUpdateEvent) event));
        } else if (event instanceof ReadyEvent) {
            onReady(((ReadyEvent) event));
        }
    }

    @SubscribeEvent
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        onGuildMessageEvent(event, event.getMessage());
    }

    @SubscribeEvent
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (!shouldEvaluateCommandOnMessageUpdate || event.getMessage().isPinned())
            return;
        onGuildMessageEvent(event, event.getMessage());
    }

    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        final JDA jda = event.getJDA();
        final List<Object> registeredListeners = jda.getRegisteredListeners();
        final IEventManager eventManager = ((JDAImpl) jda).getEventManager();
        for (Object registeredListener : registeredListeners) {
            eventManager.unregister(registeredListener);
        }
        if (eventManager instanceof InterfacedEventManager) {
            for (CommandPlugin module : modules) {
                if (module instanceof EventListener) {
                    eventManager.register(module);
                }
            }
        } else {
            for (CommandPlugin module : modules) {
                eventManager.register(module);
            }
        }
        eventManager.handle(event);
        jda.addEventListener(registeredListeners.toArray());
    }

    private void onGuildMessageEvent(GenericGuildMessageEvent event, Message message) {
        final CommandEventInternal commandEvent = eventFactory.createEvent(event, message, BreadBotImpl.this);
        if (commandEvent != null) {
            LOG.debug(commandEvent.toString());
            commandEngine.handle(commandEvent);
            ((JDAImpl) event.getJDA()).getEventManager().handle(commandEvent);
        }
    }

    public void propagateReadyEvent() {
        for (CommandPlugin commandPlugin : getPlugins()) {
            commandPlugin.onBreadReady(this);
        }

    }
}