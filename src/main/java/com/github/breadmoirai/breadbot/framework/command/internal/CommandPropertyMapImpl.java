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

import com.github.breadmoirai.breadbot.framework.annotation.InheritedProperty;
import com.github.breadmoirai.breadbot.framework.annotation.RegisterPropertyMapper;
import com.github.breadmoirai.breadbot.framework.command.CommandPropertyMap;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A builder for a {@link CommandPropertyMap}. This map can inherit values from another map.
 */
public class CommandPropertyMapImpl implements CommandPropertyMap {

    private final Map<Class<?>, Object> properties;

    public CommandPropertyMapImpl(CommandPropertyMapImpl map) {
        this(map, null);
    }

    public CommandPropertyMapImpl(CommandPropertyMapImpl defaultMap, Annotation[] annotations) {
        properties = new HashMap<>();
        if (defaultMap != null) {
            defaultMap.properties.forEach((aClass, o) -> {
                if (aClass.isAnnotationPresent(InheritedProperty.class)) {
                    properties.put(aClass, o);
                }
            });
        }
        if (annotations != null) {
            putAnnotations(annotations);
        }
    }

    @Override
    public boolean hasProperty(Class<?> propertyType) {
        return propertyType == null || properties.containsKey(propertyType);
    }

    @Override
    public <T> T getProperty(Class<T> propertyType) {
        final Object obj = properties.get(propertyType);
        if (propertyType != null)
            return propertyType.cast(obj);
        else return null;
    }

    public <T> void putProperty(Class<? super T> propertyType, T propertyObj) {
        properties.put(propertyType, propertyObj);
    }

    public void putProperty(Object propertyObj) {
        if (propertyObj instanceof Annotation) {
            final Class<? extends Annotation> aClass = ((Annotation) propertyObj).annotationType();
            properties.put(aClass, propertyObj);
        } else {
            properties.put(propertyObj.getClass(), propertyObj);
        }
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

    /**
     * Returns an {@link java.util.Iterator} of the property values in this map.
     *
     * @return an {@link java.util.Iterator}
     */

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
        final StringBuilder sb = new StringBuilder("CommandPropertyMapImpl{")
                .append(properties)
                .append('}');
        return sb.toString();
    }
}