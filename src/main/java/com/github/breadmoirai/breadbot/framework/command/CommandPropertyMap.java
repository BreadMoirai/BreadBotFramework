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

package com.github.breadmoirai.breadbot.framework.command;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public interface CommandPropertyMap extends Iterable<Object> {

    boolean hasProperty(Class<?> propertyType);

    /**
     * Retrieves the property of the passed {@link Class}. If this obj does not contain a mapping, it will attempt to provide a value from it's defaultPropertyMap
     *
     * @param propertyType a class
     * @param <T>          the type
     * @return the type if found, otherwise {@code null}
     */
    <T> T getProperty(Class<T> propertyType);

    /**
     * Checks the provided {@link Predicate} against the property type if it exists, returning the result if it does, and {@code false} if it doesn't.
     *
     * @param propertyType the property class
     * @param test         the predicate to test the property.
     * @param <T>          the type
     * @return {@code true} if the property exists and {@code test} returns {@code true}.
     */
    default <T> boolean testProperty(Class<T> propertyType, Predicate<T> test) {
        final T property = getProperty(propertyType);
        return property != null && test.test(property);
    }

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

}