/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This is used to set settings for command parameters.
 */
public interface CommandParameterBuilder {

    BreadBotBuilder getClientBuilder();

    CommandHandleBuilder getCommandBuilder();

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
     * If {@code index > 0}, the parser will only attempt to map the argument at the specified position.
     * If the position is out of bounds, this parameter will be passed {@code null}.
     * The first argument consumed will always be at this index.
     * If the width is positive and there are not enough arguments to satisfy that width starting from this index, {@code null} will be passed.
     * By default the index is set to 0 which means that there is no specific index.
     * If the index is set to a negative value, it will count backwards where the last argument is at index {@code -1}.
     *
     * @param index the starting index of the argument to consume.
     * @return this builder instance
     */
    CommandParameterBuilder setIndex(int index);

    /**
     * Sets the width of the argument, how many tokens the parse should consume.
     * By default this is set to {@code 1}, meaning it will only ever consume 1 argument or 0 if it cannot match anything.
     * If the width is set to {@code 0}, it will try every contiguous combination of arguments starting from the largest size.
     * If the width is set to a negative value, it will attempt each group of contiguous arguments.
     * If the width is set to a positive value greater that {@code 1}, it will try every group of contiguous arguments of that exact size.
     *
     * @param width the number of arguments to consume.
     * @return this builder instance
     */
    CommandParameterBuilder setWidth(int width);

    <T> CommandParameterBuilder setTypeParser(TypeParser<T> parser);

    CommandParameterBuilder setParser(ArgumentParser parser);

    /**
     * @param mustBePresent {@code true} if the argument must be present. Otherwise an error message will be sent to the user with the default error or
     * @return this
     */
    CommandParameterBuilder setRequired(boolean mustBePresent);

    /**
     * Defines the behavior to be executed when the parameter could not be mapped from any unmapped CommandEventArguments.
     *
     * @param onAbsentArgument A MissingArgumentHandler which is a functional interface that is a BiConsumer of the CommandEvent and the CommandParameter that is missing
     * @return this
     */
    CommandParameterBuilder setOnAbsentArgument(AbsentArgumentHandler onAbsentArgument);

    /**
     * This only affects parameters which are Collections.
     * If contiguous is set to {@code true}, that means that only adjacent arguments will be provided in the parameter where the first element is the first unmapped argument that is of the Collection's Generic Type.
     * If contiguous is set to {@code false}, then all unmapped arguments which can be mapped to this Collection's Generic Type will be present in this parameter.
     * <p>By default, this field is set to {@code false}.
     *
     * @param isContiguous a boolean.
     * @return this
     */
    CommandParameterBuilder setContiguous(boolean isContiguous);

    /**
     * This only affects parameters that contain multiple arguments such as a Stream or a List.
     * This field determines the maximum amount of arguments to include.
     * By default this is set to -1.
     *
     * @param limit An int determining the maximum amount of arguments to contain.
     * @return this
     */
    CommandParameterBuilder setLimit(int limit);

    CommandParameterBuilder addArgumentPredicate(Predicate<CommandArgument> argumentPredicate);

    default CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    boolean hasProperty(Class<?> propertyType);

    Method getDeclaringMethod();

    <T> T getProperty(Class<T> propertyType);

    CommandParameter build();
}