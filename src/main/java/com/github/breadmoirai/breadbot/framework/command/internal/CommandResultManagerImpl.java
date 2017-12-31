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

package com.github.breadmoirai.breadbot.framework.command.internal;

import com.github.breadmoirai.breadbot.framework.builder.CommandResultManagerBuilder;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.command.CommandResultManager;
import com.github.breadmoirai.breadbot.framework.defaults.DefaultCommandResultHandlers;
import net.dv8tion.jda.core.utils.tuple.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandResultManagerImpl implements CommandResultManager, CommandResultManagerBuilder {

    private final Map<Class<?>, CommandResultHandler<?>> map = new HashMap<>();

    public CommandResultManagerImpl() {
        new DefaultCommandResultHandlers().initialize(this);
    }

    private static int getSuperCount(Class<?> klass) {
        if (klass.getSuperclass() == null) {
            return 1;
        } else return 1 + getSuperCount(klass.getSuperclass());
    }

    @Override
    public <T> CommandResultManagerBuilder registerResultHandler(Class<T> resultType, CommandResultHandler<T> handler) {
        map.put(resultType, handler);
        return this;
    }

    @Override
    public <T> CommandResultHandler<? super T> getResultHandler(Class<T> resultType) {
        if (resultType == Object.class) return getOneHandlerForEverything();
        if (map.containsKey(resultType))
            //noinspection unchecked
            return (CommandResultHandler<? super T>) map.get(resultType);
        else if (resultType.getSuperclass() != null) {
            return getResultHandler(resultType.getSuperclass());
        } else {
            return null;
        }
    }

    private CommandResultHandler<Object> getOneHandlerForEverything() {
        Comparator<Map.Entry<Class<?>, CommandResultHandler<?>>> c = Map.Entry.comparingByKey(Comparator.comparingInt(CommandResultManagerImpl::getSuperCount));
        List<Pair<? extends Class<?>, ? extends CommandResultHandler<?>>> list = map.entrySet()
                .stream()
                .sorted(c)
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return (command, event, result) -> list.stream()
                .filter(pair -> pair.getLeft().isInstance(result))
                .findFirst()
                .ifPresent(pair -> CommandResultHandler.handleObject(pair.getRight(), command, event, result));
    }

}