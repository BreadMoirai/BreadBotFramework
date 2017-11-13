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
package com.github.breadmoirai.breadbot.framework.internal.parameter;

import com.github.breadmoirai.breadbot.framework.CommandArgumentList;

import java.util.function.BiFunction;

public class CommandParameterFunctionImpl implements CommandParameter {

    private final BiFunction<CommandArgumentList, CommandParser, ?> function;

    public CommandParameterFunctionImpl(BiFunction<CommandArgumentList, CommandParser, ?> function) {
        this.function = function;
    }

    @Override
    public Object map(CommandArgumentList list, CommandParser set) {
        return function.apply(list, set);
    }

    @Override
    public Class<?> getType() {
        return null;
    }

    @Override
    public int getFlags() {
        return 0;
    }

    @Override
    public int getIndex() {
        return -1;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public boolean isMustBePresent() {
        return false;
    }
}
