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
package com.github.breadmoirai.breadbot.framework.inject;

import com.github.breadmoirai.breadbot.util.TypeMap;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BreadInjector {

    // -- STATIC INNER CLASS -- //
    public static class Injector {

        private final Field[] fields;
        private final Object[] values;
        private int last = 0;

        private Injector(Field[] fields, Object[] values) {
            this.fields = fields;
            this.values = values;
        }

        public void inject(Object o) throws IllegalAccessException {
            if (last == o.hashCode()) return;
            for (int i = 0; i < fields.length; i++) {
                fields[i].set(o, values[i]);
            }
            last = o.hashCode();
        }
    }

    private final TypeMap map;
    private final Map<Class<?>, Injector> injectors;

    public BreadInjector(TypeMap map) {
        this.map = map;
        injectors = new HashMap<>();
    }

    public Injector getInjectorFor(Class<?> aClass) {
        final Injector classInjector = injectors.get(aClass);
        if (classInjector == null) {
            final LinkedList<Object> values = new LinkedList<>();
            final LinkedList<Field> fields = new LinkedList<>();
            for (Field field : aClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(javax.inject.Inject.class)) {
                    Object in;
                    if ((in = map.get(field.getType())) != null) {
                        fields.add(field);
                        values.add(in);
                    }
                }
            }
            if (fields.isEmpty()) {
                return null;
            } else {
                final Field[] fieldArr = fields.toArray(new Field[0]);
                final Object[] valueArr = values.toArray();
                AccessibleObject.setAccessible(fieldArr, true);
                final Injector injector = new Injector(fieldArr, valueArr);
                injectors.put(aClass, injector);
                return injector;
            }
        }
        return classInjector;
    }
}
