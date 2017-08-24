package com.github.breadmoirai.bot.framework.event.args;

import java.util.function.Predicate;

public interface ArgumentType<T> extends Predicate<CommandArgument> {

    public static final int STRICT = 0b0001;
    public static final int LOOSE = 0b0010;

    /**
     * Striciteiisgdigs
     * @param flags
     * @return
     */
    default boolean isStrict(int flags) {
        return (flags & STRICT) != 0;
    }

    default boolean isLoose(int flags) {
        return (flags & LOOSE) != 0;
    }

    /**
     * Returns {@code true} if the CommandArgument can be
     * is of this type.
     *
     * @param arg the CommandArgument
     * @param flags the flags. See Constants
     *
     * @return {@code true} if {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType#map}
     */
    boolean test(CommandArgument arg, int flags);

    /**
     * Maps the CommandArgument to this given type.
     * This method should only be called after
     * {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType#test(CommandArgument, int)}
     * returns {@code true}.
     *
     * @param arg The CommandArgument to be mapped.
     * @param flags the flags. See Class Constants*
     *
     * @return T
     */
    T map(CommandArgument arg, int flags);
}
