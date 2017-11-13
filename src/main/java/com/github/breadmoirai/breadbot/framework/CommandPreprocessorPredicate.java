/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.breadbot.framework;

import com.github.breadmoirai.breadbot.framework.internal.command.CommandProcessStack;

import java.util.function.Predicate;

/**
 * Returns {@code true} if the command should continue to execute, {@code false} otherwise.
 */
@FunctionalInterface
public interface CommandPreprocessorPredicate extends CommandPreprocessorFunction, Predicate<CommandEvent> {

    @Override
    default void process(Object commandObj, CommandHandle targetHandle, CommandEvent event, CommandProcessStack processQueue) {
        if (test(event)) {
            processQueue.runNext();
        }
    }

    /**
     * @return {@code true} if the command should continue to execute, {@code false} otherwise.
     */
    @Override
    boolean test(CommandEvent event);


}
