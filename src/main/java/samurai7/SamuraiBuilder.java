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
 *
 */
package samurai7;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import samurai7.core.IModule;
import samurai7.core.engine.CommandEngine;
import samurai7.core.engine.CommandEngineConfiguration;
import samurai7.modules.admin.AdminModule;
import samurai7.modules.prefix.PrefixModule;
import samurai7.modules.source.SourceModule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class SamuraiBuilder {

    private String prefix = "!";
    private long sourceGuild;
    private long ownerId;
    private boolean allowModifiablePrefix = true;
    private Game game;
    private List<IModule> modules;
    private boolean admin = false;
    private Consumer<CommandEngineConfiguration> config;

    public SamuraiBuilder() {
        modules = new LinkedList<>();
    }

    public SamuraiBuilder setDefaultPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Allow guilds to have specific prefixes
     * <p>By default this is set to {@code true}
     * @param allow true if yes, false if all guilds must use same prefix.
     */
    public SamuraiBuilder allowModifiablePrefix(boolean allow) {
        this.allowModifiablePrefix = allow;
        return this;
    }

    /**
     * If this is set, the {@link samurai7.modules.source.Source @Source} annotation is enabled. Command classes marked with this annotation is restricted to usage within the set guild.
     * @param guildId the guildId to restrict commands to.
     */
    public SamuraiBuilder setSourceGuild(long guildId) {
        this.sourceGuild = guildId;
        return this;
    }

    public SamuraiBuilder setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public SamuraiBuilder installModule(IModule... module) {
        Collections.addAll(modules, module);
        return this;
    }

    public SamuraiBuilder installModule(List<IModule> moduleList) {
        modules.addAll(moduleList);
        return this;
    }

    /**
     * This enables the @Admin annotation that is marked on Command classes.
     * This ensures that Commands marked with @Admin are only usable by Administrators.
     *
     * <p>By default a Member is defined as an Administrator if he/she satisfies the following conditions
     * <ul>
     *     <li>Has Kick Members Permission</li>
     *     <li>Is higher than the bot on the role hierarchy</li>
     * </ul>
     *
     * This behavior can be changed by extending the {@link samurai7.modules.admin.AdminModule} like the following:
     * <pre><code>
     * {@link samurai7.SamuraiBuilder samuraiBuilder}.addModule(new MyAdminModule());
     * ...
     * public class MyAdminModule implements {@link samurai7.modules.admin.IAdminModule} {
     *    {@literal @}Override
     *     boolean isAdmin({@link net.dv8tion.jda.core.entities.Member} member) {
     *         //new criteria
     *         //can check roles or maybe have a whitelist in the Database
     *     }
     * }
     *     </code>
     * </pre>
     */
    public SamuraiBuilder addDefaultAdminModule() {
        this.admin = true;
        return this;
    }

    public SamuraiBuilder configure(Consumer<CommandEngineConfiguration> config) {
        this.config = config;
        return this;
    }

    public JDABuilder buildJDA() {
        if (admin) modules.add(new AdminModule());
        if (sourceGuild != 0) modules.add(new SourceModule(sourceGuild));
        final PrefixModule prefixModule = new PrefixModule(prefix, allowModifiablePrefix);
        modules.add(prefixModule);

        final CommandEngineConfiguration configuration = new CommandEngineConfiguration();
        if (config != null) config.accept(configuration);
        modules.forEach(iModule -> iModule.init(configuration));

        return new JDABuilder(AccountType.BOT)
                .setEventManager(new AnnotatedEventManager())
                .setGame(game)
                .addEventListener(new CommandEngine(configuration, modules, prefixModule))
                .addEventListener(modules.toArray());
    }

}
