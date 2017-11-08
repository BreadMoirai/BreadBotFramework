package com.github.breadmoirai.breadbot.framework.internal;

import com.github.breadmoirai.breadbot.framework.command.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.command.parameter.ArgumentTypeMapper;
import com.github.breadmoirai.breadbot.framework.command.parameter.ArgumentTypePredicate;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandArgument;

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
        final ArgumentTypeMapper<T> r = (arg, flags) -> getAsType.apply(arg);
        registerArgumentMapper(type, l, r);
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
