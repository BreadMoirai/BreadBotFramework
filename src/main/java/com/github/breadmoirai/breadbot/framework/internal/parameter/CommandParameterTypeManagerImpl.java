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

package com.github.breadmoirai.breadbot.framework.internal.parameter;

import com.github.breadmoirai.breadbot.framework.builder.CommandParameterManagerBuilder;
import com.github.breadmoirai.breadbot.framework.defaults.DefaultCommandParameters;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentTypeMapper;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentTypePredicate;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameterTypeManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Does argument Mapping.
 * Is basically a heterogeneous map of Class<?> to ArgumentMapper<?>
 */
public final class CommandParameterTypeManagerImpl implements CommandParameterTypeManager, CommandParameterManagerBuilder {

    private final Map<Class<?>, ArgumentParser<?>> map;

    public CommandParameterTypeManagerImpl() {
        map = new HashMap<>();
        new DefaultCommandParameters().initialize(this);
    }

    @Override
    public <T> CommandParameterManagerBuilder registerArgumentMapper(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        put(type, new ArgumentParser<>(predicate, mapper));
        return this;
    }

    @Override
    public <T> ArgumentParser<T> getParser(Class<T> type) {
        final ArgumentParser<?> pair = map.get(type);
        if (pair != null) {
            //noinspection unchecked
            return (ArgumentParser<T>) pair;
        } else return null;
    }

    public void put(Class<?> type, ArgumentParser<?> parser) {
        map.put(type, parser);
    }
}