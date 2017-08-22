package com.github.breadmoirai.bot.util;

import com.github.breadmoirai.bot.framework.error.TypeFinderException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeFinder {

    public static Type[] getTypeArguments(Class<?> cls, final Class<?> toClass) {
        if (!toClass.isAssignableFrom(cls)) throw new TypeFinderException();
        if (toClass.equals(cls)) throw new TypeFinderException("Command registered without explicit Type Arguments");

        final Type superclass = cls.getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            return ((ParameterizedType) superclass).getActualTypeArguments();
        }
        else return getTypeArguments((Class<?>) superclass, toClass);
    }
}
