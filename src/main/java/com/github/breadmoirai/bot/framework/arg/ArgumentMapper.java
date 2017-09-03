package com.github.breadmoirai.bot.framework.arg;

import org.jetbrains.annotations.Contract;

import java.util.Optional;

/**
 * This class provided behavior to map a {@link com.github.breadmoirai.bot.framework.arg.CommandArgument} to a specified Type.
 * @param <T> the type to map to.
 */
@FunctionalInterface
public interface ArgumentMapper<T> {

    ArgumentMapper<Void> VOID_MAPPER = (arg, flags) -> Optional.empty();

    /**
     * Maps the {@link com.github.breadmoirai.bot.framework.arg.CommandArgument} to this given type.
     * This method should never return a null value.
     *
     * @param arg The {@link com.github.breadmoirai.bot.framework.arg.CommandArgument} to be mapped.
     * @param flags the flags. See Class Constants*
     *
     * @return {@code !null} value. Must be wrapped in an {@link java.util.Optional}
     */
    @Contract("_, _ -> !null")
    Optional<T> map(CommandArgument arg, int flags);

}
