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

import com.github.breadmoirai.breadbot.framework.parameter.CommandParameterTypeManager;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;

public interface CommandParameterManagerBuilder extends CommandParameterTypeManager {
    /**
     * Registers an ArgumentMapper with the type provided.
     *
     * @param type      the Type class
     * @param parser    the mapper
     * @param <T>       the type
     */
    <T> CommandParameterManagerBuilder registerParameterType(Class<T> type, TypeParser<T> parser);

}