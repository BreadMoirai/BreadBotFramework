/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentTypeMapper;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentTypePredicate;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;

import java.util.function.Function;
import java.util.function.Predicate;

public interface CommandParameterManagerBuilder<R> {
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
    <T> R registerArgumentMapper(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper);

    /**
     * This ignores flags. Use {@link CommandParameterManagerBuilder#registerArgumentMapper} otherwise.
     *
     * @param type      The type class
     * @param isType    predicate to test if the argument can be parsed to the type provided. This param can be left {@code null} if the complexity is close to {@code getAsType.apply(arg) != null}
     * @param getAsType A function to convert the argument to the type provided.
     * @param <T>       The type
     */
    default <T> R registerArgumentMapperSimple(Class<T> type, Predicate<CommandArgument> isType, Function<CommandArgument, T> getAsType) {
        final ArgumentTypePredicate l = isType == null ? null : (arg, flags) -> isType.test(arg);
        final ArgumentTypeMapper<T> r = (arg, flags) -> getAsType.apply(arg);
        return registerArgumentMapper(type, l, r);
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