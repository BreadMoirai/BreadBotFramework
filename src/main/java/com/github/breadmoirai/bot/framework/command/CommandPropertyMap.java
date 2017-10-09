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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface CommandPropertyMap extends Iterable<Object> {
    boolean containsProperty(Class<?> propertyType);

    /**
     * Retrieves the property of the passed {@link Class}. If this obj does not contain a mapping, it will attempt to provide a value from it's defaultPropertyMap
     *
     * @param propertyType a class
     * @param <T>          the type
     *
     * @return the type if found, otherwise {@code null}
     */
    <T> T getProperty(Class<T> propertyType);

    /**
     * Retrieves the property if it was declared on this element. If no element is found, it will not search for a default mapping in the enclosing object.
     *
     * @param propertyType the property class
     * @param <T>          the type
     * @return the property if found on this map, otherwise {@code null}
     */
    <T> T getDeclaredProperty(Class<T> propertyType);

    /**
     * Returns a read-only unmodifiable {@link java.util.Set} view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    Set<Map.Entry<Class<?>, Object>> entrySet();

    /**
     * Returns a read-only unmodifiable {@link java.util.Collection} view of the mappings contained in this map.
     *
     * @return a {@link java.util.Collection} view of the mappings contained in this map.
     */
    Collection<Object> values();

    /**
     * returns another propertymap.
     *
     * @return the property map for inherited/default values.
     */
    CommandPropertyMap getDefaultProperties();
}
