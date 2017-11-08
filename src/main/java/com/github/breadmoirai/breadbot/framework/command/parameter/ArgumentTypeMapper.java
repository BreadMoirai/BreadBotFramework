package com.github.breadmoirai.breadbot.framework.command.parameter;

/**
 * This class provided behavior to map a {@link com.github.breadmoirai.breadbot.framework.command.parameter.CommandArgument} to a specified Type.
 * @param <T> the type to map to.
 */
@FunctionalInterface
public interface ArgumentTypeMapper<T> {

    static final ArgumentTypeMapper<Void> VOID_MAPPER = (arg, flags) -> null;

    /**
     * Maps / Parses the {@link com.github.breadmoirai.breadbot.framework.command.parameter.CommandArgument} to this given type.
     * This method should return null if it cannot be mapped
     *
     * @param arg The {@link com.github.breadmoirai.breadbot.framework.command.parameter.CommandArgument} to be mapped.
     * @param flags the flags.
     *
     * @return {@code !null} value. Must be wrapped in an {@link java.util.Optional}
     */
    T map(CommandArgument arg, int flags);

}
