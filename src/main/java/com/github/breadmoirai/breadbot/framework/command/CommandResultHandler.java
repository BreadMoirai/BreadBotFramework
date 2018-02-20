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

package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

/**
 * This handles the result of a command, in other terms the returned value from invoking a command method
 *
 * @param <T> the result type
 */
@FunctionalInterface
public interface CommandResultHandler<T> {

    static <T> void handleObject(CommandResultHandler<T> handler, Command command, CommandEvent event, Object result) {
        //noinspection unchecked
        T cast = (T) result;
        handler.handleResult(command, event, cast);
    }

    void handleResult(Command command, CommandEvent event, T result);

}