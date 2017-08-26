package com.github.breadmoirai.bot.framework.event.args;

import org.jetbrains.annotations.Contract;

import java.util.Optional;

/**
 * This class provided behavior to map
 * @param <T>
 */
@FunctionalInterface
public interface ArgumentMapper<T> {

    /**
     * Maps the {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument} to this given type.
     * This method should never return a null value.
     *
     * @param arg The {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument} to be mapped.
     * @param flags the flags. See Class Constants*
     *
     * @return {@code !null} value. Must be wrapped in an {@link java.util.Optional}
     */
    @Contract("_, _ -> !null")
    Optional<T> map(CommandArgument arg, int flags);

}
