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

package com.github.breadmoirai.breadbot.framework.parameter.internal;

import com.github.breadmoirai.breadbot.framework.builder.CommandParameterManagerBuilder;
import com.github.breadmoirai.breadbot.framework.defaults.DefaultCommandParameters;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Does argument Mapping.
 * Is basically a heterogeneous map of Class<?> to ArgumentMapper<?>
 */
public final class CommandParameterTypeManagerImpl implements CommandParameterManagerBuilder {

    private final Map<Class<?>, TypeParser<?>> map;

    public CommandParameterTypeManagerImpl() {
        map = new HashMap<>();
        new DefaultCommandParameters().initialize(this);
    }

    @Override
    public <T> CommandParameterManagerBuilder registerParameterType(Class<T> type, TypeParser<T> parser) {
        put(type, parser);
        return this;
    }

    @Override
    public <T> TypeParser<T> getTypeParser(Class<T> type) {
        final TypeParser<?> pair = map.get(type);
        if (pair != null) {
            //noinspection unchecked
            return (TypeParser<T>) pair;
        } else return null;
    }

    public void put(Class<?> type, TypeParser<?> parser) {
        map.put(type, parser);
    }
}