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

import com.github.breadmoirai.breadbot.util.TypeHashMap;

public class InjectionBuilderImpl implements InjectionBuilder<InjectionBuilderImpl> {

    private final TypeHashMap map;

    public InjectionBuilderImpl() {
        map = new TypeHashMap();
    }

    @Override
    public <V> InjectionBuilderImpl bindInjection(V fieldValue) {
        map.put(fieldValue);
        return this;
    }

    @Override
    public <V> InjectionBuilderImpl bindInjection(Class<V> fieldType, V fieldValue) {
        map.put(fieldType, fieldValue);
        return this;
    }

    public void bindInjectionUnchecked(Object type, Object value) {
        map.getMap().put((Class<?>) type, value);
    }

    @Override
    public InjectionBuilderImpl self() {
        return this;
    }

    public BreadInjector build() {
        return new BreadInjector(map);
    }
}
