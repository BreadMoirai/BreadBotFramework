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
package com.github.breadmoirai.breadbot.framework.internal.command;

import com.github.breadmoirai.breadbot.framework.annotation.RegisterPropertyMapper;
import com.github.breadmoirai.breadbot.framework.command.CommandPropertyMap;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

/**
 * A builder for a {@link CommandPropertyMap}. This map can inherit values from another map.
 */
public class CommandPropertyMapImpl implements Iterable<Object>, CommandPropertyMap {

    private CommandPropertyMap defaultProperties;
    private final Map<Class<?>, Object> properties;

    public CommandPropertyMapImpl() {
        this(null, null);
    }

    public CommandPropertyMapImpl(CommandPropertyMap defaultMap) {
        this(defaultMap, null);
    }

    public CommandPropertyMapImpl(Annotation[] annotations) {
        this(null, annotations);
    }

    public CommandPropertyMapImpl(CommandPropertyMap defaultMap, Annotation[] annotations) {
        properties = new HashMap<>();
        this.defaultProperties = defaultMap;
        if (annotations != null) {
            putAnnotations(annotations);
        }
    }

    @Override
    public boolean hasProperty(Class<?> propertyType) {
        return properties.containsKey(propertyType) || (defaultProperties != null && defaultProperties.hasProperty(propertyType));
    }

    @Override
    public boolean hasDeclaredProperty(Class<?> propertyType) {
        return properties.containsKey(propertyType);
    }

    /**
     * Retrieves the property of the passed {@link java.lang.Class}. If this obj does not contain a mapping, it will attempt to provide a value from it's defaultPropertyMap
     *
     * @param propertyType a class
     * @param <T>          the type
     * @return the type if found, otherwise {@code null}
     */
    @Override
    public <T> T getProperty(Class<T> propertyType) {
        final Object obj = properties.get(propertyType);
        if (obj == null) {
            if (defaultProperties != null) {
                return defaultProperties.getProperty(propertyType);
            }
            else return null;
        }
        return propertyType.cast(obj);
    }

    @Override
    public <T> T getDeclaredProperty(Class<T> propertyType) {
        return propertyType.cast(properties.get(propertyType));
    }

    public <T> CommandPropertyMapImpl putProperty(Class<? super T> propertyType, T propertyObj) {
        properties.put(propertyType, propertyObj);
        return this;
    }

    public CommandPropertyMapImpl putProperty(Object propertyObj) {
        if (propertyObj instanceof Annotation) {
            final Class<? extends Annotation> aClass = ((Annotation) propertyObj).annotationType();
            properties.put(aClass, propertyObj);
        } else {
            properties.put(propertyObj.getClass(), propertyObj);
        }
        return this;
    }

    public CommandPropertyMapImpl setDefaultProperties(CommandPropertyMap defaultProperties) {
        this.defaultProperties = defaultProperties;
        return this;
    }

    public CommandPropertyMapImpl clear() {
        properties.clear();
        return this;
    }

    /**
     * Returns a read-only unmodifiable {@link java.util.Set} view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    @Override
    public Set<Map.Entry<Class<?>, Object>> entrySet() {
        return Collections.unmodifiableSet(properties.entrySet());
    }

    /**
     * Returns a read-only unmodifiable {@link java.util.Collection} view of the mappings contained in this map.
     *
     * @return a {@link java.util.Collection} view of the mappings contained in this map.
     */
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

        return (defaultProperties != null ? defaultProperties.equals(objects.defaultProperties) : objects.defaultProperties == null) && (properties != null ? properties.equals(objects.properties) : objects.properties == null);
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

    public CommandPropertyMapImpl putAnnotations(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            final RegisterPropertyMapper propertyMapper = annotation.annotationType().getAnnotation(RegisterPropertyMapper.class);
            if (propertyMapper != null) {
                final Class<? extends Function<? extends Annotation, ?>> mapper = propertyMapper.mapper();
                try {
                    final Function<? extends Annotation, ?> function = mapper.newInstance();
                    final Object o;
                    try {
                        o = applyAnnotationMapper(function, annotation);
                    } catch (ClassCastException e) {
                        throw new RuntimeException("The Function Class specified by @RegisterPropertyMapper annotated to " + annotation.getClass().getName() + " is of the wrong type. The function should have the generic parameters <" + annotation.getClass().getName() + ", ?>.", e);
                    }

                    final Class<?> type = propertyMapper.type();
                    if (type != Object.class)
                        try {
                            putSupertypedProperty(type, o.getClass(), o);
                        } catch (ClassCastException e) {
                            throw new RuntimeException("The parameter \"type\" specified by @RegisterPropertyMapper annotated to " + annotation.getClass().getName() + " is of the wrong type. The type should be a supertype of " + o.getClass());
                        }
                    else
                        putProperty(o);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Could not instantiate property mapper " + mapper.getName(), e);
                }
            } else putProperty(annotation);
        }
        return this;
    }

    private <T extends Annotation> Object applyAnnotationMapper(Function<T, ?> function, Annotation annotation) {
        @SuppressWarnings("unchecked") final T cast = (T) annotation;
        return function.apply(cast);
    }

    private <T> void putSupertypedProperty(Class<T> type, Class<?> objClass, Object obj) {
        if (type.isAssignableFrom(objClass)) {
            putProperty(type, type.cast(obj));
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommandPropertyMapImpl{");
        sb.append("defaultProperties=").append(defaultProperties);
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }
}