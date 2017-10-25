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
package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.ICommandModule;
import com.github.breadmoirai.breadbot.framework.command.CommandProperties;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.ICommandEventFactory;
import com.github.breadmoirai.breadbot.framework.event.impl.CommandEventFactoryImpl;
import com.github.breadmoirai.breadbot.framework.impl.BreadBotClientImpl;
import com.github.breadmoirai.breadbot.modules.admin.DefaultAdminModule;
import com.github.breadmoirai.breadbot.modules.prefix.DefaultPrefixModule;
import com.github.breadmoirai.breadbot.modules.prefix.IPrefixModule;
import com.github.breadmoirai.breadbot.modules.source.SourceModule;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BreadBotClientBuilder {

    private static final Logger LOG = LoggerFactory.getLogger("CommandClient");

    private final List<ICommandModule> modules;
    private ICommandEventFactory commandEventFactory;
    private CommandProperties commandProperties;
    private Predicate<Message> preProcessPredicate;
    private CommandHandleBuilderFactoryImpl factory;

    public BreadBotClientBuilder() {
        commandProperties = new CommandProperties();
        modules = new ArrayList<>();
        factory = new CommandHandleBuilderFactoryImpl(this);
    }

    public BreadBotClientBuilder addModule(ICommandModule... modules) {
        for (ICommandModule module : modules) {
            module.initialize(this);
        }
        return this;
    }

    public BreadBotClientBuilder addModule(Collection<ICommandModule> modules) {
        for (ICommandModule module : modules) {
            addModule(module);
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


    /**
     * This module provides a static prefix that cannot be changed. By default, the prefix is set to "!".
     *
     * <p>This method's implementation is:
     * <pre><code> {@link BreadBotClientBuilder#addModule(ICommandModule...) addModule}(new {@link DefaultPrefixModule DefaultPrefixModule}(prefix)) </code></pre>
     *
     * <p>You can define a different prefix implementation by providing a class to {@link BreadBotClientBuilder#addModule(ICommandModule...)} that implements {@link IPrefixModule IPrefixModule}
     */
    public BreadBotClientBuilder addDefaultPrefixModule(String prefix) {
        addModule(new DefaultPrefixModule(prefix));
        return this;
    }

    /**
     * This enables the {@link com.github.breadmoirai.breadbot.modules.admin.Admin @Admin} annotation that is marked on Command classes.
     * This ensures that Commands marked with {@link com.github.breadmoirai.breadbot.modules.admin.Admin @Admin} are only usable by Administrators.
     * <p>It is <b>important</b> to include an implementation of {@link com.github.breadmoirai.breadbot.modules.admin.IAdminModule IAdminModule} through either this method, {@link BreadBotClientBuilder#addAdminModule(Predicate)}, or your own implementation.
     * Otherwise, all users will have access to Administrative Commands
     * <p>
     * <p>The default criteria for defining an Administrator is as follows:
     * <ul>
     * <li>Has Kick Members Permission</li>
     * <li>Is higher than the bot on the role hierarchy</li>
     * </ul>
     * <p>
     * <p>Different criteria to determine which member has administrative status with {@link BreadBotClientBuilder#addAdminModule(Predicate)}
     * or your own implementation of {@link com.github.breadmoirai.breadbot.modules.admin.IAdminModule}
     */
    public BreadBotClientBuilder addDefaultAdminModule() {
        addModule(new DefaultAdminModule());
        return this;
    }

    /**
     * Define custom behavior to determine which members can use Commands marked with {@link com.github.breadmoirai.breadbot.modules.admin.Admin @Admin}
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link BreadBotClientBuilder#addModule(ICommandModule...) addModule}(new {@link DefaultAdminModule DefaultAdminModule}(isAdmin)) </code></pre>
     */
    public BreadBotClientBuilder addAdminModule(Predicate<Member> isAdmin) {
        addModule(new DefaultAdminModule(isAdmin));
        return this;
    }

    /**
     * Adding this module will enable {@link com.github.breadmoirai.breadbot.modules.source.SourceGuild @SourceGuild} annotations on Commands.
     *
     * @param sourceGuildId The guild id
     */
    public BreadBotClientBuilder addSourceModule(long sourceGuildId) {
        addModule(new SourceModule(sourceGuildId));
        return this;
    }

    public BreadBotClientBuilder addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        factory.addCommand(onCommand, configurator);
        return this;
    }

    public BreadBotClientBuilder addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        factory.addCommand(commandClass, configurator);
        return this;
    }

    public BreadBotClientBuilder addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        factory.addCommand(commandObject, configurator);
        return this;
    }

    public BreadBotClientBuilder addCommand(Supplier<Object> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        factory.addCommand(commandSupplier, configurator);
        return this;
    }

    public BreadBotClientBuilder addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        factory.addCommands(packageName, configurator);
        return this;
    }

    public CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        return factory.createCommand(onCommand);
    }

    public CommandHandleBuilder createCommand(Supplier<Object> commandSupplier) {
        return factory.createCommand(commandSupplier);
    }

    public CommandHandleBuilder createCommand(Class<?> commandClass) {
        return factory.createCommand(commandClass);
    }

    public CommandHandleBuilder createCommand(Object commandObject) {
        return factory.createCommand(commandObject);
    }

    public List<CommandHandleBuilder> createCommands(String packageName) {
        return factory.createCommands(packageName);
    }

    public List<CommandHandleBuilder> getBuilderList() {
        return factory.getBuilderList();
    }

    public BreadBotClientBuilder setPreProcessPredicate(Predicate<Message> predicate) {
        preProcessPredicate = predicate;
        return this;
    }

    public BreadBotClientBuilder addPreProcessPredicate(Predicate<Message> predicate) {
        if (preProcessPredicate == null) {
            preProcessPredicate = predicate;
        } else {
            preProcessPredicate = preProcessPredicate.and(predicate);
        }
        return this;
    }

    public CommandProperties getCommandProperties() {
        return commandProperties;
    }

    /**
     * Not much use for this at the moment.
     */
    public BreadBotClientBuilder setEventFactory(ICommandEventFactory commandEventFactory) {
        this.commandEventFactory = commandEventFactory;
        return this;
    }

    /**
     * Builds a BreadBotClient with an {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager}
     *
     * This implementation is as follows:
     * <pre><code>
     *     return {@link BreadBotClientBuilder breadBotBuilder}.{@link BreadBotClientBuilder#build build}(new {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager()});
     * </code></pre>
     *
     * @return The {@link com.github.breadmoirai.breadbot.framework.BreadBotClient} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager}({@link com.github.breadmoirai.breadbot.framework.BreadBotClient#getEventManager client.getEventManager()})
     */
    public BreadBotClient buildAnnotated() {
        return build(new AnnotatedEventManager());
    }

    /**
     * Builds a BreadBotClient with an {@link net.dv8tion.jda.core.hooks.InterfacedEventManager}
     *
     * This implementation is as follows:
     * <pre><code>
     *     return {@link BreadBotClientBuilder breadBotBuilder}.{@link BreadBotClientBuilder#build build}(new {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventManager()});
     * </code></pre>
     *
     * @return The {@link com.github.breadmoirai.breadbot.framework.BreadBotClient} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager}({@link com.github.breadmoirai.breadbot.framework.BreadBotClient#getEventManager client.getEventManager()})
     */
    public BreadBotClient buildInterfaced() {
        return build(new InterfacedEventManager());
    }

    /**
     * Builds the BreadBotClient with the provided EventManager.
     * It is at this point that all Modules are initialized and Commands built.
     * If an {@link com.github.breadmoirai.breadbot.modules.prefix.IPrefixModule} has not been provided, a {@link com.github.breadmoirai.breadbot.modules.prefix.DefaultPrefixModule new DefaultPrefixModule("!")} is provided.
     * @param eventManager The IEventManager of which to attach all the listeners (CommandModules) to. If the module is an instanceof {@link net.dv8tion.jda.core.hooks.InterfacedEventManager} it will only use {@link IEventManager#register(Object)} on Modules that extend {@link net.dv8tion.jda.core.hooks.EventListener}. Otherwise, the BreadBotClient will register all the CommandModules as listeners.
     * @return a new BreadBotClient.
     */
    public BreadBotClient build(IEventManager eventManager) {
        if (!hasModule(IPrefixModule.class)) modules.add(new DefaultPrefixModule("!"));
        if (commandEventFactory == null) commandEventFactory = new CommandEventFactoryImpl(getModule(IPrefixModule.class));

        BreadBotClient client = new BreadBotClientImpl(modules, eventManager, commandEventFactory, factory, preProcessPredicate);
        LOG.info("Top Level Commands registered: " + client.getCommandMap().values().size() + ".");
        LOG.info("CommandClient Initialized.");
        return client;
    }

}