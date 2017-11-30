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

package com.github.breadmoirai.breadbot.framework.parameter;

/**
 * This class provided behavior to map a {@link CommandArgument} to a specified Type.
 * @param <T> the type to map to.
 */
@FunctionalInterface
public interface ArgumentTypeMapper<T> {

    ArgumentTypeMapper<Void> VOID_MAPPER = (arg, flags) -> null;

    /**
     * Maps / Parses the {@link CommandArgument} to this given type.
     * This method should return null if it cannot be mapped
     *
     * @param arg The {@link CommandArgument} to be mapped.
     * @param flags the flags.
     *
     * @return {@code !null} value. Must be wrapped in an {@link java.util.Optional}
     */
    T map(CommandArgument arg, int flags);

}
