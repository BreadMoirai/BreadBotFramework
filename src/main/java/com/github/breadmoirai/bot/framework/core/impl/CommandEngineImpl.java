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
package com.github.breadmoirai.bot.framework.core.impl;

import com.github.breadmoirai.bot.framework.core.CommandEngine;
import com.github.breadmoirai.bot.framework.core.CommandEvent;
import com.github.breadmoirai.bot.framework.core.IModule;
import com.github.breadmoirai.bot.framework.core.command.CommandHandle;
import com.github.breadmoirai.bot.framework.core.command.ICommand;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CommandEngineImpl implements CommandEngine {

    public static final SimpleLog LOG = SimpleLog.getLog("CommandEngine");

    private final Map<String, CommandHandle> commandMap;

    private final Predicate<ICommand> postProcessPredicate;

    public CommandEngineImpl(List<IModule> modules, Map<String, CommandHandle> commandMap, Predicate<ICommand> postProcessPredicate) {
        this.commandMap = commandMap;
        this.postProcessPredicate = postProcessPredicate == null ? iCommand -> true : postProcessPredicate;
    }

    @Override
    public void execute(CommandEvent event) {
        CommandHandle command;
        final String key = event.getKey().toLowerCase();

        command = commandMap.get(key);
        if (command != null) {
            try {
                command.execute(event);
            } catch (Throwable throwable) {
                LOG.fatal(throwable);
            }
        }
    }

    @Override
    public Class<? extends ICommand> getCommandClass(String key) {
        return commandMap.get(key);
    }

    @Override
    public void log(ReflectiveOperationException e) {
        LOG.fatal("Command could not be instantiated: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
    }

    @Override
    public Stream<Class<? extends ICommand>> getCommands() {
        return null;
    }

    @Override
    public boolean hasCommand(String key) {
        return commandMap.containsKey(key);
    }
}
