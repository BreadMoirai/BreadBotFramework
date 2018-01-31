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

import com.github.breadmoirai.breadbot.framework.parameter.CommandParameterManager;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;

import java.util.function.Consumer;

public interface CommandParameterManagerBuilder extends CommandParameterManager {
    /**
     * Registers a TypeParser with the type provided.
     *
     * @param type   the Type class
     * @param parser the mapper
     * @param <T>    the type
     * @return this
     */
    <T> CommandParameterManagerBuilder bindTypeParser(Class<T> type, TypeParser<T> parser);


    CommandParameterManagerBuilder clearTypeModifiers(Class<?> parameterType);

    /**
     * Assigns a Consumer to modify all parameters with the specified type.
     * This is done before any property modifiers are applied.
     *
     * @param parameterType The class of the parameter's type
     * @param modifier      a Consumer that takes the ParameterBuilder as its argument
     * @return this
     */
    CommandParameterManagerBuilder bindTypeModifier(Class<?> parameterType, Consumer<CommandParameterBuilder> modifier);
}