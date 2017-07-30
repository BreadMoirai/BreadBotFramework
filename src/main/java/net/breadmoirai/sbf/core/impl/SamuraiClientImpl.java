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

import net.breadmoirai.sbf.core.*;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class SamuraiClientImpl implements SamuraiClient {

    private JDA jda;

    private final List<IModule> modules;
    private final IEventManager eventManager;
    private final ICommandEventFactory eventFactory;
    private final CommandEngine commandEngine;

    public SamuraiClientImpl(List<IModule> modules, IEventManager eventManager, ICommandEventFactory eventFactory, CommandEngineBuilder engineBuilder) {
        this.modules = modules;
        eventManager.register(this.new SamuraiEventListener(engineBuilder.getPreProcessPredicate()));
        modules.forEach(module -> module.init(engineBuilder, this));

        if (eventManager instanceof InterfacedEventManager)
            modules.stream().filter(net.dv8tion.jda.core.hooks.EventListener.class::isInstance).forEach(eventManager::register);
        else
            modules.forEach(eventManager::register);
        this.eventManager = eventManager;
        this.eventFactory = eventFactory;
        this.commandEngine = engineBuilder.build();
    }


    @Override
    public boolean hasModule(String moduleName) {
        return moduleName != null && modules.stream().map(IModule::getName).anyMatch(moduleName::equalsIgnoreCase);
    }

    @Override
    public boolean hasModule(Class<? extends IModule> moduleClass) {
        return moduleClass != null && modules.stream().map(Object::getClass).anyMatch(moduleClass::equals);
    }

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleClass The class of the Module to find
     * @return Optional containing the module if found.
     */
    @Contract("_->!null")
    @Override
    public <T extends IModule> Optional<T> getModule(Class<T> moduleClass) {
        //noinspection unchecked
        return moduleClass == null ? Optional.empty() : modules.stream().filter(module -> moduleClass.isAssignableFrom(module.getClass())).map(iModule -> (T) iModule).findAny();
    }

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleName the name of the module to find. If the module does not override {@link net.breadmoirai.sbf.core.IModule#getName IModule#getName} the name of the class is used.
     * @return Optional containing the module if foundd.
     */
    @Contract("_->!null")
    @Override
    public Optional<IModule> getModule(String moduleName) {
        return moduleName == null ? Optional.empty() : modules.stream().filter(module -> module.getName().equalsIgnoreCase(moduleName)).findAny();
    }

    @Override
    public CommandEngine getCommandEngine() {
        return commandEngine;
    }

    @Override
    public void send(Response response) {
        send(response.getChannelId(), response);
    }

    @Override
    public void send(long channeId, Response response) {
        TextChannel textChannel = jda.getTextChannelById(channeId);
        if (textChannel == null) return;
        send(textChannel, response);
    }

    @Override
    public void send(TextChannel textChannel, Response response) {
        Objects.requireNonNull(textChannel, "TextChannel");
        Objects.requireNonNull(response, "Response");
        response.base(0, textChannel.getIdLong(), textChannel.getGuild().getIdLong(), 0, this);
        response.send(textChannel);
    }

    @Override
    public void send(User user, Response response) {
        Objects.requireNonNull(user, "User");
        Objects.requireNonNull(user, "Response");
        response.setClient(this);
        user.openPrivateChannel().queue(response::send);
    }


    private class SamuraiEventListener extends ListenerAdapter {

        private final Predicate<Message> preProcessPredicate;

        public SamuraiEventListener(Predicate<Message> preProcessPredicate) {
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
                    final Optional<Response> response = commandEngine.execute(commandEvent);
                    response.ifPresent(r -> r.setClient(SamuraiClientImpl.this));
                    response.ifPresent(SamuraiClientImpl.this::send);
                    eventManager.handle(commandEvent);
                }
            }
        }
    }
}
