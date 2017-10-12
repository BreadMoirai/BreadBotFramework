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

import com.github.breadmoirai.bot.framework.command.CommandProperties;
import com.github.breadmoirai.bot.framework.command.builder.CommandClassBuilder;
import com.github.breadmoirai.bot.framework.command.builder.FunctionalCommandBuilder;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import com.github.breadmoirai.bot.framework.event.ICommandEventFactory;
import com.github.breadmoirai.bot.framework.event.impl.CommandEventFactoryImpl;
import com.github.breadmoirai.bot.framework.impl.BreadBotClientImpl;
import com.github.breadmoirai.bot.modules.admin.DefaultAdminModule;
import com.github.breadmoirai.bot.modules.prefix.DefaultPrefixModule;
import com.github.breadmoirai.bot.modules.prefix.IPrefixModule;
import com.github.breadmoirai.bot.modules.source.SourceModule;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BreadBotClientBuilder {

    private static final Logger LOG = LoggerFactory.getLogger("CommandClient");

    private final List<ICommandModule> modules;
    private ICommandEventFactory commandEventFactory;
    private final CommandEngineBuilder commandEngineBuilder;
    private BreadBotClient client;
    private CommandProperties commandProperties;

    public BreadBotClientBuilder() {
        modules = new ArrayList<>();
        commandEngineBuilder = new CommandEngineBuilder(modules);
    }

    public BreadBotClientBuilder addModule(ICommandModule... module) {
        Collections.addAll(modules, module);
        return this;
    }

    public BreadBotClientBuilder addModule(Collection<ICommandModule> moduleList) {
        modules.addAll(moduleList);
        return this;
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
     * This enables the {@link com.github.breadmoirai.bot.modules.admin.Admin @Admin} annotation that is marked on Command classes.
     * This ensures that Commands marked with {@link com.github.breadmoirai.bot.modules.admin.Admin @Admin} are only usable by Administrators.
     * <p>It is <b>important</b> to include an implementation of {@link com.github.breadmoirai.bot.modules.admin.IAdminModule IAdminModule} through either this method, {@link BreadBotClientBuilder#addAdminModule(Predicate)}, or your own implementation.
     * Otherwise, all users will have access to Administrative Commands
     * <p>
     * <p>The default criteria for defining an Administrator is as follows:
     * <ul>
     * <li>Has Kick Members Permission</li>
     * <li>Is higher than the bot on the role hierarchy</li>
     * </ul>
     * <p>
     * <p>Different criteria to determine which member has administrative status with {@link BreadBotClientBuilder#addAdminModule(Predicate)}
     * or your own implementation of {@link com.github.breadmoirai.bot.modules.admin.IAdminModule}
     */
    public BreadBotClientBuilder addDefaultAdminModule() {
        addModule(new DefaultAdminModule());
        return this;
    }

    /**
     * Define custom behavior to determine which members can use Commands marked with {@link com.github.breadmoirai.bot.modules.admin.Admin @Admin}
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link BreadBotClientBuilder#addModule(ICommandModule...) addModule}(new {@link DefaultAdminModule DefaultAdminModule}(isAdmin)) </code></pre>
     */
    public BreadBotClientBuilder addAdminModule(Predicate<Member> isAdmin) {
        addModule(new DefaultAdminModule(isAdmin));
        return this;
    }

    /**
     * Adding this module will enable {@link com.github.breadmoirai.bot.modules.source.SourceGuild @SourceGuild} annotations on Commands.
     *
     * @param sourceGuildId The guild id
     */
    public BreadBotClientBuilder addSourceModule(long sourceGuildId) {
        addModule(new SourceModule(sourceGuildId));
        return this;
    }

    public BreadBotClientBuilder addPreProcessPredicate(Predicate<Message> predicate) {
        commandEngineBuilder.addPreProcessPredicate(predicate);
        return this;
    }

    public Predicate<Message> getPreProcessPredicate() {
        return commandEngineBuilder.getPreProcessPredicate();
    }

    public BreadBotClientBuilder registerCommand(String name, Consumer<CommandEvent> commandFunction, String... keys) {
        commandEngineBuilder.registerCommand(name, commandFunction, keys);
        return this;
    }

    public BreadBotClientBuilder registerCommand(Consumer<CommandEvent> commandFunction, Consumer<FunctionalCommandBuilder> configurator) {
        commandEngineBuilder.registerCommand(commandFunction, configurator);
		return this;
    }

    public BreadBotClientBuilder registerCommand(Object command) {
        commandEngineBuilder.registerCommand(command);
		return this;
    }

    public BreadBotClientBuilder registerCommand(Object command, Consumer<CommandClassBuilder> configurator) {
        commandEngineBuilder.registerCommand(command, configurator);
		return this;
    }

    public BreadBotClientBuilder registerCommand(Class<?> commandClass) {
        commandEngineBuilder.registerCommand(commandClass);
		return this;
    }

    public BreadBotClientBuilder registerCommand(Class<?> commandClass, Consumer<CommandClassBuilder> configurator) {
        commandEngineBuilder.registerCommand(commandClass, configurator);
		return this;
    }

    public BreadBotClientBuilder registerCommand(String packageName, Consumer<CommandClassBuilder> configurator) {
        commandEngineBuilder.registerCommand(packageName, configurator);
		return this;
    }







    public CommandProperties getCommandPropreties() {
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
     *     return {@link com.github.breadmoirai.bot.framework.BreadBotClientBuilder breadBotBuilder}.{@link com.github.breadmoirai.bot.framework.BreadBotClientBuilder#build build}(new {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager()});
     * </code></pre>
     *
     * @return The {@link com.github.breadmoirai.bot.framework.BreadBotClient} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager}({@link com.github.breadmoirai.bot.framework.BreadBotClient#getEventManager client.getEventManager()})
     */
    public BreadBotClient buildAnnotated() {
        return build(new AnnotatedEventManager());
    }

    /**
     * Builds a BreadBotClient with an {@link net.dv8tion.jda.core.hooks.InterfacedEventManager}
     *
     * This implementation is as follows:
     * <pre><code>
     *     return {@link com.github.breadmoirai.bot.framework.BreadBotClientBuilder breadBotBuilder}.{@link com.github.breadmoirai.bot.framework.BreadBotClientBuilder#build build}(new {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventManager()});
     * </code></pre>
     *
     * @return The {@link com.github.breadmoirai.bot.framework.BreadBotClient} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager}({@link com.github.breadmoirai.bot.framework.BreadBotClient#getEventManager client.getEventManager()})
     */
    public BreadBotClient buildInterfaced() {
        return build(new InterfacedEventManager());
    }

    /**
     * Builds the BreadBotClient with the provided EventManager.
     * It is at this point that all Modules are initialized and Commands built.
     * If an {@link com.github.breadmoirai.bot.modules.prefix.IPrefixModule} has not been provided, a {@link com.github.breadmoirai.bot.modules.prefix.DefaultPrefixModule new DefaultPrefixModule("!")} is provided.
     * @param eventManager The IEventManager of which to attach all the listeners (CommandModules) to. If the module is an instanceof {@link net.dv8tion.jda.core.hooks.InterfacedEventManager} it will only use {@link IEventManager#register(Object)} on Modules that extend {@link net.dv8tion.jda.core.hooks.EventListener}. Otherwise, the BreadBotClient will register all the CommandModules as listeners.
     * @return a new BreadBotClient.
     */
    public BreadBotClient build(IEventManager eventManager) {
        if (!commandEngineBuilder.hasModule(IPrefixModule.class)) modules.add(new DefaultPrefixModule("!"));
        if (commandEventFactory == null) commandEventFactory = new CommandEventFactoryImpl(commandEngineBuilder);
        BreadBotClient client = new BreadBotClientImpl(modules, eventManager, commandEventFactory, commandEngineBuilder, preprocessors);
        LOG.info("Top Level Commands registered: " + client.getCommandEngine().getCommandMap().values().size() + ".");
        LOG.info("CommandClient Initialized.");
        return client;
    }

}