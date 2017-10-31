package com.github.breadmoirai.breadbot.framework.internal;

import com.github.breadmoirai.breadbot.framework.command.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.command.parameter.ArgumentTypeMapper;
import com.github.breadmoirai.breadbot.framework.command.parameter.ArgumentTypePredicate;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandArgument;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ArgumentTypes {
    /**
     * Registers an ArgumentMapper with the type provided.
     *
     * @param type   the Type class
     *
     * @param predicate This returns {@code true} if the {@link CommandArgument} can be mapped to the {@code type}.
     *                  If the computation cost is similar to mapping the argument, leave this field null.
     * @param mapper the mapper
     * @param <T>    the type
     */
    <T> void registerArgumentMapper(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper);

    /**
     * This ignores flags. Use {@link ArgumentTypes#registerArgumentMapper} otherwise.
     *
     * @param type      The type class
     * @param isType    predicate to test if the argument can be parsed to the type provided. This param can be left {@code null} if the complexity is close to {@code getAsType.apply(arg) != null}
     * @param getAsType A function to convert the argument to the type provided.
     * @param <T>       The type
     */
    default <T> void registerArgumentMapperSimple(Class<T> type, Predicate<CommandArgument> isType, Function<CommandArgument, T> getAsType) {
        final ArgumentTypePredicate l = isType == null ? null : (arg, flags) -> isType.test(arg);
        final ArgumentTypeMapper<T> r = (arg, flags) -> Optional.ofNullable(getAsType.apply(arg));
        registerArgumentMapper(type, l, r);
    }

    /**
     * Attempts to map the CommandArgument to the type provided using the flags and a registered ArgumentMapper.
     * If an ArgumentMapper is not registered with the type requested, an empty Optional is returned.
     *
     * @param type  the intended type class
     * @param arg   the argument to map from
     * @param flags any {@link com.github.breadmoirai.breadbot.framework.command.parameter.ArgumentFlags flags}
     * @param <T>   the type
     * @return an Optional containing the result if successful. Otherwise empty.
     */
    default <T> Optional<T> getAsType(Class<T> type, CommandArgument arg, int flags) {
        final ArgumentParser<T> parser = getParser(type);
        if (parser == null) return Optional.empty();
        return parser.parse(arg, flags);
    }

    /**
     * Attempts to map the CommandArgument to the type provided using the flags and a registered ArgumentMapper.
     * If an ArgumentMapper is not registered with the type requested, an empty Optional is returned.
     *
     * @param type the intended type class
     * @param arg  the argument to map from
     * @param <T>  the type
     * @return an Optional containing the result if successful. Otherwise empty.
     */
    default <T> Optional<T> getAsType(Class<T> type, CommandArgument arg) {
        return getAsType(type, arg, 0);
    }

    /**
     * Checks if the passed arg is
     * <pre><code>
     *     {@link ArgumentTypes#getAsType(Class, CommandArgument, int) getAsType(type, arg, flags)}.isPresent();
     * </code></pre>
     */
    default boolean isOfType(Class<?> type, CommandArgument arg, int flags) {
        final ArgumentParser<?> parser = getParser(type);
        if (parser == null) {
            return false;
        }
        return parser.test(arg, flags);
    }

    /**
     * Implemented as
     * <pre><code>
     *     {@link ArgumentTypes#isOfType(Class, CommandArgument, int) isOfType(type, arg, 0)}
     * </code></pre>
     */
    default boolean isOfType(Class<?> type, CommandArgument arg) {
        return isOfType(type, arg, 0);
    }

    /**
     * Returns the predicate mapper pair registered if found.
     *
     * @param type the class of the type as it was registered or one of the default types.
     * @param <T>  the type
     * @return an ArgumentParser if found. Else {@code null}.
     */
    <T> ArgumentParser<T> getParser(Class<T> type);
}
