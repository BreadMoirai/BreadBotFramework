package com.github.breadmoirai.bot.framework.arg;

import gnu.trove.set.TIntSet;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

public interface ArgumentParameter {

    Object map(CommandArgumentList list, CommandArgumentList.ArgumentIterator itr, TIntSet set);

    static ArgumentParameter ofParam(Parameter param) {
        Class<?> paramType;
        if (Optional.class == param.getType()) {
            ParameterizedType parameterizedType = ((ParameterizedType) param.getParameterizedType());
            Type type = parameterizedType.getActualTypeArguments()[0];
            paramType = (Class<?>) type;

        } else {

        }
    }

}
