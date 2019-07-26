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

package com.github.breadmoirai.breadbot.framework.parameter.internal.builder;

import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterManagerBuilder;
import com.github.breadmoirai.breadbot.framework.defaults.DefaultCommandParameters;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Does argument Mapping.
 *
 * Is a heterogeneous map of {@literal Class<?> to ArgumentMapper<?>}
 */
public final class CommandParameterTypeManagerImpl implements CommandParameterManagerBuilder<CommandParameterTypeManagerImpl> {

    private final Map<Class<?>, TypeParser<?>> map;
    private final Map<Class<?>, Consumer<CommandParameterBuilder>> map2;

    public CommandParameterTypeManagerImpl() {
        map = new HashMap<>();
        map2 = new HashMap<>();
        new DefaultCommandParameters().initialize(this);
    }

    @Override
    public <T> CommandParameterTypeManagerImpl bindTypeParser(Class<T> type, TypeParser<T> parser) {
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

    @Override
    public CommandParameterTypeManagerImpl clearTypeModifiers(Class<?> parameterType) {
        map2.remove(parameterType);
        return this;
    }

    @Override
    public CommandParameterTypeManagerImpl bindTypeModifier(Class<?> parameterType, Consumer<CommandParameterBuilder> modifier) {
        if (!map2.containsKey(parameterType)) {
            map2.put(parameterType, modifier);
        } else {
            final Consumer<CommandParameterBuilder> consumer = map2.get(parameterType);
            map2.put(parameterType, consumer.andThen(modifier));
        }
        return this;
    }

    @Override
    public void applyTypeModifiers(CommandParameterBuilder parameterBuilder) {
        final Consumer<CommandParameterBuilder> c = map2.get(parameterBuilder.getDeclaringParameter().getType());
        if (c != null) {
            c.accept(parameterBuilder);
        }
    }

    /**
     * This is a self reference for internal convenience.
     *
     * @return a reference to this object
     */
    @Override
    public CommandParameterTypeManagerImpl self() {
        return this;
    }
}