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
package com.github.breadmoirai.bot.framework.impl;

import com.github.breadmoirai.bot.framework.CommandEngine;
import com.github.breadmoirai.bot.framework.IModule;
import com.github.breadmoirai.bot.framework.command.ICommand;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.List;
import java.util.Map;

public class CommandEngineImpl implements CommandEngine {

    public static final SimpleLog LOG = SimpleLog.getLog("CommandEngine");

    private final Map<String, ICommand> commandMap;

    public CommandEngineImpl(List<IModule> modules, Map<String, ICommand> commandMap) {
        this.commandMap = commandMap;
    }

    @Override
    public void handle(CommandEvent event) {
        ICommand command;
        final String key = event.getKey().toLowerCase();
        command = commandMap.get(key);
        if (command != null) {
            try {
                command.handle(event);
            } catch (Throwable throwable) {
                LOG.fatal(throwable);
            }
        }
    }

    @Override
    public boolean hasCommand(String key) {
        return commandMap.containsKey(key);
    }
}
