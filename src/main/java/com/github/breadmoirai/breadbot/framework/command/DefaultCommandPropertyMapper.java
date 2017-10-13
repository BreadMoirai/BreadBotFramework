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
package com.github.breadmoirai.breadbot.framework.command;

import java.util.function.Function;

public class DefaultCommandPropertyMapper implements Function<DefaultCommand, Command> {


    /**
     * Applies this function to the given argument.
     *
     * @param defaultCommand the function argument
     * @return the function result
     */
    @Override
    public Command apply(DefaultCommand defaultCommand) {
        return get();
    }


    private static Command empty;

    @Command("")
    private static Command get() {
        if (empty == null) {
            try {
                empty = DefaultCommandPropertyMapper.class.getDeclaredMethod("get").getDeclaredAnnotation(Command.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return empty;
    }
}
