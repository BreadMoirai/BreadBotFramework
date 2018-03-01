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

package com.github.breadmoirai.breadbot.framework;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.CommandEngine;
import com.github.breadmoirai.breadbot.framework.command.CommandResultManager;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameterManager;

import java.util.List;
import java.util.Map;

public interface BreadBot {

    /**
     * Checks for a plugin with the specified class or superclass.
     *
     * @param pluginClass The class of the plugin or a superclass of it.
     * @return {@code true} if a plugin that can be casted to the {@code pluginClass} is found.
     */
    boolean hasPlugin(Class<? extends CommandPlugin> pluginClass);

    /**
     * Retrieves a plugin with the specified class.
     * Specifically, iterates through the list of CommandPlugins and if a plugin that can be casted to {@code T} is
     * found, it is casted and returned.
     *
     * @param pluginClass the class of the plugin
     * @param <T> the type of the plugin
     * @return the plugin if found, otherwise {@code null}
     */
    <T extends CommandPlugin> T getPlugin(Class<T> pluginClass);

    /**
     * Retrieves the list of all added plugins.
     *
     * @return a non-null immutable list.
     */
    List<CommandPlugin> getPlugins();

    /**
     * Retrieves the functional object that evaluates CommandEvents.
     *
     * @return the implemented CommandEngine
     */
    CommandEngine getCommandEngine();

    CommandParameterManager getArgumentTypes();

    CommandResultManager getResultManager();

    Map<String, Command> getCommandMap();
}