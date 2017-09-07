package com.github.breadmoirai.bot.framework.arg;

import gnu.trove.set.TIntSet;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public interface CommandParameter {

    Object map(CommandArgumentList list, TIntSet set);

    static CommandParameter ofParam(Parameter param) {
        Class<?> paramType;
        if (Optional.class == param.getType()) {
            ParameterizedType parameterizedType = ((ParameterizedType) param.getParameterizedType());
            Type type = parameterizedType.getActualTypeArguments()[0];
            paramType = (Class<?>) type;

        } else if (List.class == param.getType()) {

        } else {

        }

    }

}
