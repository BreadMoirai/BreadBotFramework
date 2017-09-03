package com.github.breadmoirai.bot.framework.arg;

import com.github.breadmoirai.bot.framework.event.CommandEvent;
import gnu.trove.set.TIntSet;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface ArgumentParameter {

    /**
     * Sets the flags to be passed to the {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper}
     *
     * @param flags
     */
    void setFlags(int flags);

    /**
     * Setting this value indicates that this {@link com.github.breadmoirai.bot.framework.arg.ArgumentParameter} should only map the {@link com.github.breadmoirai.bot.framework.arg.CommandArgument} at the specified index.
     * If multiple indexes are passed, this will combine the specified indexes into a single argument and attempt to map from that.
     *
     * @param indexes index of the argument beginning at 0.
     */
    void setIndex(int... indexes);

    /**
     * Sets the intended base type of the method. \\todo make a wiki
     *
     * @param type the Class of the argument.
     */
    void setType(Class<?> type);

    /**
     * Sets the {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper} to be used in mapping the {@link com.github.breadmoirai.bot.framework.arg.ArgumentParameter}.
     * If an {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper} is registered in {@link com.github.breadmoirai.bot.framework.arg.ArgumentTypes}, it will not be used.
     * The provided {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper} will not be registered with {@link com.github.breadmoirai.bot.framework.arg.ArgumentTypes}.
     * It is generally recommended to prefer using different {@link com.github.breadmoirai.bot.framework.arg.ArgumentFlags flags} on custom types to indicate that the {@link com.github.breadmoirai.bot.framework.arg.CommandArgument} should be mapped differently.
     *
     * @param mapper a public class that implements {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper} and contains a no-args public constructor.
     */
    void setTypeMapper(Class<? extends ArgumentMapper> mapper);

    /**
     * @param mustBePresent {@code true} if the argument must be present. Otherwise an error message will be sent to the user with the default error or
     */
    void setOptional(boolean mustBePresent);


    void setOnParamNotFound(BiConsumer<CommandEvent, ArgumentParameter> onParamNotFound);


    Object map(CommandArgumentList list, TIntSet set);

    static ArgumentParameter ofParam(Parameter param) {
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
