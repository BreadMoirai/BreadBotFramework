/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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

import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.plugins.admin.AdminPlugin;
import com.github.breadmoirai.breadbot.plugins.admin.AdminPluginImpl;
import com.github.breadmoirai.breadbot.plugins.owner.ApplicationOwnerPlugin;
import com.github.breadmoirai.breadbot.plugins.owner.StaticOwnerPlugin;
import com.github.breadmoirai.breadbot.plugins.prefix.PrefixPlugin;
import com.github.breadmoirai.breadbot.plugins.prefix.UnmodifiablePrefixPlugin;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.Checks;

import java.util.Collection;
import java.util.function.Predicate;

public interface CommandPluginBuilder {

    /**
     * Adds a module and initializes it.
     *
     * @param module an instance of {@link CommandPlugin CommandModule}.
     * @return this
     */
    CommandPluginBuilder addPlugin(CommandPlugin module);

    /**
     * Adds a collection of modules and initializes each one.
     *
     * @param modules a Collection of CommandModules. Should not contain any null elements.
     * @return this
     */
    CommandPluginBuilder addPlugin(Collection<CommandPlugin> modules);

    /**
     * Checks whether there is a module present that is of the class provided or a subclass of it.
     *
     * @param moduleClass a class
     * @return {@code true} if a module has been added that is assignable to {@code moduleClass}, {@code null} otherwise.
     */
    boolean hasPlugin(Class<? extends CommandPlugin> moduleClass);

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleClass The class of the Module to find
     * @return The module if found. Else {@code null}.
     */
    <M extends CommandPlugin> M getPlugin(Class<M> moduleClass);

    /**
     * This adds a module that implements {@link PrefixPlugin} to provide a static prefix that cannot be changed. If a {@link PrefixPlugin} is not added, one will be provided with a static prefix of {@code "!"}
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link CommandPluginBuilder#addPlugin(CommandPlugin) addModule}(new {@link UnmodifiablePrefixPlugin DefaultPrefixModule}(prefix)) </code></pre>
     *
     * <p>You can define a different prefix implementation by providing an object to {@link BreadBotClientBuilder#addPlugin(CommandPlugin) addModule(ICommandModule)} that implements {@link PrefixPlugin IPrefixModule}
     *
     * @param prefix a string the defines a global prefix
     * @return this
     */
    default CommandPluginBuilder addStaticPrefix(String prefix) {
        return addPlugin(new UnmodifiablePrefixPlugin(prefix));
    }

    /**
     * This enables the {@link com.github.breadmoirai.breadbot.plugins.admin.Admin @Admin} annotation that is marked on Command classes.
     * This ensures that Commands marked with {@link com.github.breadmoirai.breadbot.plugins.admin.Admin @Admin} are only usable by Administrators.
     * <p>It is <b>important</b> to include an implementation of {@link AdminPlugin AdminModule} through either this method, {@link BreadBotClientBuilder#addAdminPlugin(Predicate)}, or your own implementation.
     * Otherwise, all users will have access to Administrative Commands
     * <p>
     * <p>The default criteria for defining an Administrator is as follows:
     * <ul>
     * <li>Has Kick Members Permission</li>
     * <li>Is higher than the bot on the role hierarchy</li>
     * </ul>
     * <p>
     * <p>Different criteria to determine which member has administrative status with {@link BreadBotClientBuilder#addAdminPlugin(Predicate)}
     * or your own implementation of {@link AdminPlugin}
     *
     * @return this
     */
    default CommandPluginBuilder addAdminPlugin() {
        return addPlugin(new AdminPluginImpl());
    }

    /**
     * Define custom behavior to determine which members can use Commands marked with {@link com.github.breadmoirai.breadbot.plugins.admin.Admin @Admin}
     * <p>
     * <p>This method's implementation is:
     * <pre><code> {@link #addPlugin(CommandPlugin) addModule}(new {@link AdminPluginImpl DefaultAdminModule}(isAdmin)) </code></pre>
     *
     * @param isAdmin a Predicate that can determine which members are Admins
     * @return this
     */
    default CommandPluginBuilder addAdminPlugin(Predicate<Member> isAdmin) {
        Checks.notNull(isAdmin, "isAdmin");
        return addPlugin(new AdminPluginImpl(isAdmin));
    }

    /**
     * Allows the {@link com.github.breadmoirai.breadbot.plugins.owner.Owner @Owner} annotation to be used.
     * If one or more ids are provided as an argument to this method, then only those id's are recognized as being able to use a command marked with @Owner.
     * If no ids are provided to this method, the only owner recognized is the application owner.
     *
     * @param owners the ids of the owners
     * @return this
     */
    default CommandPluginBuilder addOwnerPlugin(long... owners) {
        if (owners.length == 0) {
            addPlugin(new ApplicationOwnerPlugin());
        } else {
            addPlugin(new StaticOwnerPlugin(owners));
        }
        return this;
    }


}
