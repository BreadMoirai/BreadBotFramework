package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.CommandModule;
import com.github.breadmoirai.breadbot.modules.admin.AdminModule;
import com.github.breadmoirai.breadbot.modules.admin.DefaultAdminModule;
import com.github.breadmoirai.breadbot.modules.prefix.DefaultPrefixModule;
import com.github.breadmoirai.breadbot.modules.prefix.PrefixModule;
import com.github.breadmoirai.breadbot.modules.source.GuildRestrictionModule;
import com.github.breadmoirai.breadbot.modules.source.RestrictToGuild;
import net.dv8tion.jda.core.entities.Member;

import java.util.Collection;
import java.util.function.Predicate;

public interface CommandModuleBuilder<T> {

    /**
     * Adds a module and initializes it.
     *
     * @param module an instance of {@link CommandModule CommandModule}.
     * @return this
     */
    T addModule(CommandModule module);

    /**
     * Adds a collection of modules and initializes each one.
     *
     * @param modules a Collection of CommandModules. Should not contain any null elements.
     * @return this
     */
    T addModule(Collection<CommandModule> modules);

    /**
     * Checks whether there is a module present that is of the class provided or a subclass of it.
     *
     * @param moduleClass a class
     * @return {@code true} if a module has been added that is assignable to {@code moduleClass}, {@code null} otherwise.
     */
    boolean hasModule(Class<? extends CommandModule> moduleClass);

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleClass The class of the Module to find
     * @return The module if found. Else {@code null}.
     */
    <M extends CommandModule> M getModule(Class<M> moduleClass);

    /**
     * This adds a module that implements {@link PrefixModule} to provide a static prefix that cannot be changed. If a {@link PrefixModule} is not added, one will be provided with a static prefix of {@code "!"}
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link CommandModuleBuilder#addModule(CommandModule) addModule}(new {@link DefaultPrefixModule DefaultPrefixModule}(prefix)) </code></pre>
     *
     * <p>You can define a different prefix implementation by providing an object to {@link BreadBotClientBuilder#addModule(CommandModule) addModule(ICommandModule)} that implements {@link PrefixModule IPrefixModule}
     *
     * @param prefix a string the defines a global prefix
     * @return this
     */
    default T addDefaultPrefixModule(String prefix) {
        return addModule(new DefaultPrefixModule(prefix));
    }

    /**
     * This enables the {@link com.github.breadmoirai.breadbot.modules.admin.Admin @Admin} annotation that is marked on Command classes.
     * This ensures that Commands marked with {@link com.github.breadmoirai.breadbot.modules.admin.Admin @Admin} are only usable by Administrators.
     * <p>It is <b>important</b> to include an implementation of {@link AdminModule AdminModule} through either this method, {@link BreadBotClientBuilder#addAdminModule(Predicate)}, or your own implementation.
     * Otherwise, all users will have access to Administrative Commands
     * <p>
     * <p>The default criteria for defining an Administrator is as follows:
     * <ul>
     * <li>Has Kick Members Permission</li>
     * <li>Is higher than the bot on the role hierarchy</li>
     * </ul>
     * <p>
     * <p>Different criteria to determine which member has administrative status with {@link BreadBotClientBuilder#addAdminModule(Predicate)}
     * or your own implementation of {@link AdminModule}
     *
     * @return this
     */
    default T addDefaultAdminModule() {
        return addModule(new DefaultAdminModule());
    }

    /**
     * Define custom behavior to determine which members can use Commands marked with {@link com.github.breadmoirai.breadbot.modules.admin.Admin @Admin}
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link #addModule(CommandModule) addModule}(new {@link DefaultAdminModule DefaultAdminModule}(isAdmin)) </code></pre>
     *
     * @return this
     */
    default T addAdminModule(Predicate<Member> isAdmin) {
        return addModule(new DefaultAdminModule(isAdmin));
    }

    /**
     * Adding this module will enable {@link RestrictToGuild @RestrictToGuild} annotations on Commands.
     * This ensures that some commands can only be used in certain guilds.
     *
     * @param sourceGuildId The guild id
     * @return this
     */
    default T addSourceModule(long sourceGuildId) {
        return addModule(new GuildRestrictionModule(sourceGuildId));
    }


}
