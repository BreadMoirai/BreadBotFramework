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
package com.github.breadmoirai.bot.framework.command.impl;

import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * A Heterogeneous map of {@link Class} to {@link Object}. This map can inherit values from another map.
 */
public class CommandPropertyMapImpl implements CommandPropertyMap {

    private final CommandPropertyMap defaultProperties;
    private final Map<Class<?>, Object> properties;

    public CommandPropertyMapImpl(CommandPropertyMap defaultProperties, Map<Class<?>, Object> properties) {
        this.defaultProperties = defaultProperties;
        this.properties = properties;
    }

    @Override
    public boolean containsProperty(Class<?> propertyType) {
        return (properties.containsKey(propertyType) || defaultProperties != null) && defaultProperties.containsProperty(propertyType);
    }

    @Override
    public <T> T getProperty(Class<T> propertyType) {
        final Object obj = properties.get(propertyType);
        if (obj == null) {
            if (defaultProperties != null)
                return defaultProperties.getProperty(propertyType);
            else return null;
        }
        return propertyType.cast(obj);
    }

    @Override
    public <T> T getDeclaredProperty(Class<T> propertyType) {
        return propertyType.cast(properties.get(propertyType));
    }

    @Override
    public Set<Map.Entry<Class<?>, Object>> entrySet() {
        return Collections.unmodifiableSet(properties.entrySet());
    }

    @Override
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(properties.values());
    }

    @Override
    public CommandPropertyMap getDefaultProperties() {
        return defaultProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandPropertyMapImpl objects = (CommandPropertyMapImpl) o;

        if (defaultProperties != null ? !defaultProperties.equals(objects.defaultProperties) : objects.defaultProperties != null)
            return false;
        return properties != null ? properties.equals(objects.properties) : objects.properties == null;
    }

    @Override
    public int hashCode() {
        int result = defaultProperties != null ? defaultProperties.hashCode() : 0;
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    /**
     * Returns an {@link Iterator} of the property values in this map.
     *
     * @return an {@link Iterator}
     */
    @NotNull
    @Override
    public Iterator<Object> iterator() {
        return values().iterator();
    }
}
