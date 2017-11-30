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

import com.github.breadmoirai.breadbot.framework.internal.parameter.CommandParameterTypeManagerImpl;
import com.github.breadmoirai.breadbot.framework.parameter.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.function.Consumer;

public interface CommandParameterBuilder {

    /**
     * @return the {@link java.lang.reflect.Parameter}
     */
    Parameter getDeclaringParameter();

    /**
     * Sets the name of this parameter. Primarily used for debugging.
     * If {@code -parameters} is not passed to javac, parameter names will not be included in the compiled code and thus appear as {@code arg0, arg1, arg2, ...}, otherwise it will appear as in your code.
     *
     * @param paramName the name of the parameter.
     * @return this obj.
     */
    CommandParameterBuilder setName(String paramName);

    /**
     * Sets the flags to be passed to the {@link ArgumentTypeMapper}
     *
     * @param flags
     */
    CommandParameterBuilder setFlags(int flags);

    /**
     * If the index is known, the parser will only attempt to map the argument at the specified position.
     * If number of arguments is less than the index  The default value of {@code -1}
     *
     * @param index
     * @return
     */
    CommandParameterBuilder setIndex(int index);

    /**
     * Sets the width of the argument, how many tokens the parse should consume.
     *
     * @param width
     * @return
     */
    CommandParameterBuilder setWidth(int width);

    /**
     * Sets the intended base type of the method. \\todo make a wiki
     *
     * @param type the Class of the argument.
     */
    <T> CommandParameterBuilder setBaseType(Class<T> type);

    /**
     * Sets the {@link ArgumentTypeMapper} to be used in mapping the {@link CommandParameter}.
     * If an {@link ArgumentTypeMapper} is registered in {@link CommandParameterTypeManagerImpl}, it will not be used.
     * The provided {@link ArgumentTypeMapper} will not be registered with {@link CommandParameterTypeManagerImpl}.
     * It is generally recommended to prefer using different {@link ArgumentFlags flags} on custom types to indicate that the {@link CommandArgument} should be mapped differently.
     *
     * @param mapper a public class that implements {@link ArgumentTypeMapper} and contains a no-args public constructor.
     */
    default <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentTypeMapper<T> mapper) {
        return setBaseType(type, null, mapper);
    }

    <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper);

    /**
     * @param predicate may be null
     * @param parser    may not be null
     * @return this
     */
    <T> CommandParameterBuilder setParser(@Nullable ArgumentTypePredicate predicate, ArgumentTypeMapper<T> parser);

    /**
     * @param mustBePresent {@code true} if the argument must be present. Otherwise an error message will be sent to the user with the default error or
     * @return this
     */
    CommandParameterBuilder setRequired(boolean mustBePresent);

    /**
     * Defines the behavior to be executed when the parameter could not be mapped from any unmapped CommandEventArguments.
     *
     * @param onParamNotFound A MissingArgumentHandler which is a functional interface that is a BiConsumer of the CommandEvent and the CommandParameter that is missing
     * @return this
     */
    CommandParameterBuilder setOnAbsentArgument(AbsentArgumentHandler onParamNotFound);

    /**
     * This only affects parameters which are Collections.
     * If contiguous is set to {@code true}, that means that only adjacent arguments will be provided in the parameter where the first element is the first unmapped argument that is of the Collection's Generic Type.
     * If contiguous is set to {@code false}, then all unmapped arguments which can be mapped to this Collection's Generic Type will be present in this parameter.
     * <p>By default, this field is set to {@code false}.
     * @param isContiguous a boolean.
     * @return this
     */
    CommandParameterBuilder setContiguous(boolean isContiguous);

    ArgumentParser<?> getParser();

    CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator);

    CommandParameter build();

    <T> T getProperty(Class<T> propertyType);

    boolean hasProperty(Class<?> propertyType);
}