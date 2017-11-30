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

package com.github.breadmoirai.breadbot.util;

import com.github.breadmoirai.breadbot.framework.error.TypeFinderException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeFinder {

    public static Type[] getTypeArguments(Class<?> cls, final Class<?> toClass) {
        if (!toClass.isAssignableFrom(cls)) throw new TypeFinderException();
        if (toClass.equals(cls)) throw new TypeFinderException("Command registered without explicit Type Arguments");

        final Type superclass = cls.getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            return ((ParameterizedType) superclass).getActualTypeArguments();
        } else return getTypeArguments((Class<?>) superclass, toClass);
    }
}
