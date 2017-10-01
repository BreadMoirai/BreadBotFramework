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

import com.github.breadmoirai.bot.framework.command.builder.CommandBuilder;
import com.github.breadmoirai.bot.framework.command.builder.FunctionalCommandBuilder;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import com.github.breadmoirai.bot.framework.event.ICommandEventFactory;
import com.github.breadmoirai.bot.framework.event.impl.CommandEventFactoryImpl;
import com.github.breadmoirai.bot.framework.impl.CommandClientImpl;
import com.github.breadmoirai.bot.modules.admin.Admin;
import com.github.breadmoirai.bot.modules.admin.DefaultAdminModule;
import com.github.breadmoirai.bot.modules.admin.IAdminModule;
import com.github.breadmoirai.bot.modules.prefix.DefaultPrefixModule;
import com.github.breadmoirai.bot.modules.prefix.IPrefixModule;
import com.github.breadmoirai.bot.modules.source.SourceGuild;
import com.github.breadmoirai.bot.modules.source.SourceModule;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommandClientBuilder {

    private static final Logger LOG = LoggerFactory.getLogger("CommandClient");
    private List<ICommandModule> modules;
    private ICommandEventFactory commandEventFactory;
    private Consumer<CommandEngineBuilder> commandEngineModifier;
    private CommandClient client;

    public CommandClientBuilder() {
        modules = new ArrayList<>();
    }

    public CommandClientBuilder addModule(ICommandModule... module) {
        Collections.addAll(modules, module);
        return this;
    }

    public CommandClientBuilder addModule(Collection<ICommandModule> moduleList) {
        modules.addAll(moduleList);
        return this;
    }

    /**
     * This module provides a static prefix that cannot be changed. By default, the prefix is set to "!".
     *
     * <p>This method's implementation is:
     * <pre><code> {@link CommandClientBuilder#addModule(ICommandModule...) addModule}(new {@link DefaultPrefixModule DefaultPrefixModule}(prefix)) </code></pre>
     *
     * <p>You can define a different prefix implementation by providing a class to {@link CommandClientBuilder#addModule(ICommandModule...)} that implements {@link IPrefixModule IPrefixModule}
     */
    public CommandClientBuilder addDefaultPrefixModule(String prefix) {
        addModule(new DefaultPrefixModule(prefix));
        return this;
    }

    /**
     * This enables the {@link Admin @Admin} annotation that is marked on Command classes.
     * This ensures that Commands marked with {@link Admin @Admin} are only usable by Administrators.
     * <p>It is <b>important</b> to include an implementation of {@link IAdminModule IAdminModule} through either this method, {@link CommandClientBuilder#addAdminModule(Predicate)}, or your own implementation.
     * Otherwise, all users will have access to Administrative Commands
     * <p>
     * <p>The default criteria for defining an Administrator is as follows:
     * <ul>
     * <li>Has Kick Members Permission</li>
     * <li>Is higher than the bot on the role hierarchy</li>
     * </ul>
     * <p>
     * <p>Different criteria to determine which member has administrative status with {@link CommandClientBuilder#addAdminModule(Predicate)}
     * or your own implementation of {@link IAdminModule}
     */
    public CommandClientBuilder addDefaultAdminModule() {
        addModule(new DefaultAdminModule());
        return this;
    }

    /**
     * Define custom behavior to determine which members can use Commands marked with {@link Admin @Admin}
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link CommandClientBuilder#addModule(ICommandModule...) addModule}(new {@link DefaultAdminModule DefaultAdminModule}(isAdmin)) </code></pre>
     */
    public CommandClientBuilder addAdminModule(Predicate<Member> isAdmin) {
        addModule(new DefaultAdminModule(isAdmin));
        return this;
    }

    /**
     * Adding this module will enable {@link SourceGuild @SourceGuild} annotations on Commands.
     *
     * @param sourceGuildId The guild id
     */
    public CommandClientBuilder addSourceModule(long sourceGuildId) {
        addModule(new SourceModule(sourceGuildId));
        return this;
    }

    /**
     * Modifies the CommandEngineBuilder with the given Consumer
     */
    public CommandClientBuilder configure(Consumer<CommandEngineBuilder> consumer) {
        if (commandEngineModifier == null) {
            commandEngineModifier = consumer;
        } else {
            commandEngineModifier = commandEngineModifier.andThen(consumer);
        }
        return this;
    }

    public CommandClientBuilder registerCommand(String name, Consumer<CommandEvent> commandFunction, String... keys) {
        configure(o -> o.registerCommand(name, commandFunction, keys));
        return this;
    }

    public CommandClientBuilder registerCommand(Consumer<CommandEvent> commandFunction, Consumer<FunctionalCommandBuilder> configurator) {
        configure(o -> o.registerCommand(commandFunction, configurator));
        return this;
    }

    public CommandClientBuilder registerCommand(Object command) {
        configure(o -> o.registerCommand(command));
        return this;
    }

    public CommandClientBuilder registerCommand(Object command, Consumer<CommandBuilder> configurator) {
        configure(o -> o.registerCommand(command, configurator));
        return this;
    }


    public CommandClientBuilder registerCommand(Class<?> commandClass) {
        configure(o -> o.registerCommand(commandClass));
        return this;
    }

    public CommandClientBuilder registerCommand(Class<?> commandClass, Consumer<CommandBuilder> configurator) {
        configure(o -> o.registerCommand(commandClass, configurator));
        return this;
    }

    public CommandClientBuilder registerCommand(String packageName, Consumer<CommandBuilder> configurator) {
        configure(o -> o.registerCommand(packageName, configurator));
        return this;
    }

    /**
     * Not much use for this at the moment.
     */
    public CommandClientBuilder setEventFactory(ICommandEventFactory commandEventFactory) {
        this.commandEventFactory = commandEventFactory;
        return this;
    }

    /**
     * Builds a SamuraiClient with an {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager}
     *
     * @return An {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager(IEventManager)}.
     */
    public AnnotatedEventManager buildAnnotated() {
        return build(new AnnotatedEventManager());
    }

    /**
     * Builds a SamuraiClient with an {@link net.dv8tion.jda.core.hooks.InterfacedEventManager}
     *
     * @return An {@link net.dv8tion.jda.core.hooks.InterfacedEventManager} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager(IEventManager)}.
     */
    public InterfacedEventManager buildInterfaced() {
        return build(new InterfacedEventManager());
    }

    private <T extends IEventManager> T build(T eventManager) {
        final CommandEngineBuilder commandEngineBuilder = new CommandEngineBuilder(modules);
        if (!commandEngineBuilder.hasModule(IPrefixModule.class)) modules.add(new DefaultPrefixModule("!"));
        if (commandEventFactory == null) commandEventFactory = new CommandEventFactoryImpl(commandEngineBuilder);
        commandEngineModifier.accept(commandEngineBuilder);
        this.client = new CommandClientImpl(modules, eventManager, commandEventFactory, commandEngineBuilder);
        LOG.info("Top Level Commands registered: " + client.getCommandEngine().getCommandMap().values().size() + ".");
        LOG.info("CommandClient Initialized.");
        return eventManager;
    }

    /**
     * Returns the client if it has been built. otherwise returns {@code null}
     *
     * @return A CommandClient.
     */
    public CommandClient getClient() {
        return client;
    }


}
