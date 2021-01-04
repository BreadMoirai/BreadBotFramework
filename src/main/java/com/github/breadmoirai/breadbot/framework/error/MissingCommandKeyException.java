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

package com.github.breadmoirai.breadbot.framework.error;

import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;

public class MissingCommandKeyException extends BreadBotException {
    public MissingCommandKeyException(CommandHandleBuilder handle) {
        super(String.format("A key has not been defined for Command: %s",
                handle.getDeclaringClass() == null
                        ? handle.getDeclaringObject().getClass().getSimpleName()
                        : handle.getDeclaringClass().getSimpleName()));
    }
}
