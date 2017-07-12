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

import com.github.breadmoirai.samurai7.core.ICommandEventFactory;
import com.github.breadmoirai.samurai7.core.IModule;
import com.github.breadmoirai.samurai7.core.SamuraiClient;
import com.github.breadmoirai.samurai7.modules.admin.DefaultAdminModule;
import com.github.breadmoirai.samurai7.modules.prefix.DefaultPrefixModule;
import com.github.breadmoirai.samurai7.modules.prefix.IPrefixModule;
import com.github.breadmoirai.samurai7.modules.source.SourceModule;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class SamuraiClientBuilder {

    private List<IModule> modules;
    private ICommandEventFactory commandEventFactory;
    private SamuraiClientImpl samuraiClient;

    public SamuraiClientBuilder() {
        modules = new ArrayList<>();
    }

    public SamuraiClientBuilder addModule(IModule... module) {
        Collections.addAll(modules, module);
        return this;
    }

    public SamuraiClientBuilder addModule(Collection<IModule> moduleList) {
        modules.addAll(moduleList);
        return this;
    }

    /**
     * Define the default prefix of new guilds. If this is not set, a {@link com.github.breadmoirai.samurai7.modules.prefix.DefaultPrefixModule DefaultPrefixModule} with prefix {@code !} is added.
     * <p>
     * <p>This module comes with a {@link com.github.breadmoirai.samurai7.modules.prefix.PrefixCommand PrefixCommand} that allows members to check the prefix of the guild and allows administrators to change the prefix.
     * <p>Make Sure to include an <b>AdminModule</b> through {@link SamuraiClientBuilder#addAdminModule} or {@link SamuraiClientBuilder#addAdminModule(Predicate)} otherwise the guild prefix can be changed by any member.
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link SamuraiClientBuilder#addModule(IModule...) addModule}(new {@link com.github.breadmoirai.samurai7.modules.prefix.DefaultPrefixModule DefaultPrefixModule}(prefix)) </code></pre>
     *
     * <p>You can define a different prefix implementation by providing a class to {@link SamuraiClientBuilder#addModule(IModule...)} that implements {@link com.github.breadmoirai.samurai7.modules.prefix.IPrefixModule IPrefixModule}
     */
    public SamuraiClientBuilder addDefaultPrefixModule(String prefix) {
        addModule(new DefaultPrefixModule(prefix));
        return this;
    }

    /**
     * This enables the {@link com.github.breadmoirai.samurai7.modules.admin.Admin @Admin} annotation that is marked on Command classes.
     * This ensures that Commands marked with {@link com.github.breadmoirai.samurai7.modules.admin.Admin @Admin} are only usable by Administrators.
     * <p>It is <b>important</b> to include an implementation of {@link com.github.breadmoirai.samurai7.modules.admin.IAdminModule IAdminModule} through either this method, {@link SamuraiClientBuilder#addAdminModule(Predicate)}, or your own implementation.
     * Otherwise, all users will have access to Administrative Commands
     * <p>
     * <p>The default criteria for defining an Administrator is as follows:
     * <ul>
     * <li>Has Kick Members Permission</li>
     * <li>Is higher than the bot on the role hierarchy</li>
     * </ul>
     * <p>
     * <p>Different criteria to determine which member has administrative status with {@link SamuraiClientBuilder#addAdminModule(Predicate)}
     * or your own implementation of {@link com.github.breadmoirai.samurai7.modules.admin.IAdminModule}
     */
    public SamuraiClientBuilder addDefaultAdminModule() {
        addModule(new DefaultAdminModule());
        return this;
    }

    /**
     * Define custom behavior to determine which members can use Commands marked with {@link com.github.breadmoirai.samurai7.modules.admin.Admin @Admin}
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link SamuraiClientBuilder#addModule(IModule...) addModule}(new {@link com.github.breadmoirai.samurai7.modules.admin.DefaultAdminModule DefaultAdminModule}(isAdmin)) </code></pre>
     */
    public SamuraiClientBuilder addAdminModule(Predicate<Member> isAdmin) {
        addModule(new DefaultAdminModule(isAdmin));
        return this;
    }

    /**
     * Adding this module will enable {@link com.github.breadmoirai.samurai7.modules.source.SourceGuild @SourceGuild} annotations on Commands.
     *
     * @param sourceGuildId
     */
    public SamuraiClientBuilder addSourceModule(long sourceGuildId) {
        addModule(new SourceModule(sourceGuildId));
        return this;
    }


    /**
     * Not much use for this at the moment.
     */
    public SamuraiClientBuilder setEventFactory(ICommandEventFactory commandEventFactory) {
        this.commandEventFactory = commandEventFactory;
        return this;
    }

    /**
     * Builds a SamuraiClient with an {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager}
     * @return An {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager(IEventManager)}.
     *
     * The {@link com.github.breadmoirai.samurai7.core} can be retrieved after this command is called with getClient
     */
    public AnnotatedEventManager buildAnnotated() {
        return build(new AnnotatedEventManager());
    }

    /**
     * Builds a SamuraiClient with an {@link net.dv8tion.jda.core.hooks.InterfacedEventManager}
     * @return An {@link net.dv8tion.jda.core.hooks.InterfacedEventManager} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager(IEventManager)}.
     */
    public InterfacedEventManager buildInterfaced() {
        return build(new InterfacedEventManager());
    }

    private <T extends IEventManager> T build(T eventManager) {
        final CommandEngineBuilder commandEngineBuilder = new CommandEngineBuilder(modules);
        if (!commandEngineBuilder.hasModule(IPrefixModule.class)) modules.add(new DefaultPrefixModule("!"));
        if (commandEventFactory == null) commandEventFactory = new CommandEventFactoryImpl(commandEngineBuilder);
        samuraiClient = new SamuraiClientImpl(modules, eventManager, commandEventFactory, commandEngineBuilder);
        return eventManager;
    }

    public SamuraiClient getClient() {
        return samuraiClient;
    }
}