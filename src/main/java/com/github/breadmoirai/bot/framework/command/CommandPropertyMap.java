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
package com.github.breadmoirai.bot.framework.command;

import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * A Heterogeneous map of {@link java.lang.Class} to {@link java.lang.Object}. This map can inherit values from another map.
 */
public class CommandPropertyMap implements Iterable<Object> {

    private final CommandPropertyMap defaultProperties;
    private final Map<Class<?>, Object> properties;

    CommandPropertyMap(CommandPropertyMap defaultProperties, Map<Class<?>, Object> properties) {
        this.defaultProperties = defaultProperties;
        this.properties = properties;
    }

    public boolean containsProperty(Class<?> propertyType) {
        return (properties.containsKey(propertyType) || defaultProperties != null) && defaultProperties.containsProperty(propertyType);
    }

    /**
     * Retrieves the property of the passed {@link java.lang.Class}. If this obj does not contain a mapping, it will attempt to provide a value from it's defaultPropertyMap
     *
     * @param propertyType a class
     * @param <T>          the type
     * @return the type if found, otherwise {@code null}
     */
    public <T> T getProperty(Class<T> propertyType) {
        final Object obj = properties.get(propertyType);
        if (obj == null) {
            if (defaultProperties != null)
                return defaultProperties.getProperty(propertyType);
            else return null;
        }
        return propertyType.cast(obj);
    }

    /**
     * Returns a read-only unmodifiable {@link java.util.Set} view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set<Map.Entry<Class<?>, Object>> entrySet() {
        return Collections.unmodifiableSet(properties.entrySet());
    }

    /**
     * Returns a read-only unmodifiable {@link java.util.Collection} view of the mappings contained in this map.
     *
     * @return a {@link java.util.Collection} view of the mappings contained in this map.
     */
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(properties.values());
    }

    /**
     * returns another propertymap.
     *
     * @return the property map for inherited/default values.
     */
    public CommandPropertyMap getDefaultProperties() {
        return defaultProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandPropertyMap that = (CommandPropertyMap) o;

        if (defaultProperties != null ? !defaultProperties.equals(that.defaultProperties) : that.defaultProperties != null)
            return false;
        return properties != null ? properties.equals(that.properties) : that.properties == null;
    }

    @Override
    public int hashCode() {
        int result = defaultProperties != null ? defaultProperties.hashCode() : 0;
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    /**
     * Returns an {@link java.util.Iterator} of the property values in this map.
     *
     * @return an {@link java.util.Iterator}
     */
    @NotNull
    @Override
    public Iterator<Object> iterator() {
        return values().iterator();
    }
}
