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

import com.github.breadmoirai.breadbot.framework.command.CommandEngine;
import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.command.CommandResultManager;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameterManager;
import net.dv8tion.jda.core.JDA;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface BreadBotClient {

    boolean hasModule(String pluginName);

    boolean hasModule(Class<? extends CommandPlugin> pluginClass);

    <T extends CommandPlugin> T getPlugin(Class<T> pluginClass);

    CommandPlugin getPlugin(String pluginName);

    CommandPlugin getPlugin(Type pluginType);

    List<CommandPlugin> getPlugins();

    CommandEngine getCommandEngine();

    JDA getJDA();

    void setJDA(JDA jda);

    CommandParameterManager getArgumentTypes();

    CommandResultManager getResultManager();

    Map<String, CommandHandle> getCommandMap();
}