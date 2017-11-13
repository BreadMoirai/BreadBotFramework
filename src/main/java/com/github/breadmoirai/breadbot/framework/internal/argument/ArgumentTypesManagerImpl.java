/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.breadbot.framework.internal.argument;

import com.github.breadmoirai.breadbot.framework.ArgumentTypesBuilder;
import com.github.breadmoirai.breadbot.framework.ArgumentTypesManager;
import com.github.breadmoirai.breadbot.framework.defaults.DefaultArgumentTypes;
import com.github.breadmoirai.breadbot.framework.internal.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.internal.parameter.ArgumentTypeMapper;
import com.github.breadmoirai.breadbot.framework.internal.parameter.ArgumentTypePredicate;

import java.util.HashMap;
import java.util.Map;

/**
 * Does argument Mapping.
 * Is basically a heterogeneous map of Class<?> to ArgumentMapper<?>
 */
public final class ArgumentTypesManagerImpl implements ArgumentTypesManager, ArgumentTypesBuilder<Void> {

    private final Map<Class<?>, ArgumentParser<?>> map;

    public ArgumentTypesManagerImpl() {
        map = new HashMap<>();
        new DefaultArgumentTypes().initialize(this);
    }

    @Override
    public <T> Void registerArgumentMapper(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        put(type, new ArgumentParser<>(predicate, mapper));
        return null;
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
