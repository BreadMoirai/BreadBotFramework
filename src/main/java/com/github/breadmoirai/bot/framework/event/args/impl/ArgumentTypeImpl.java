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
package com.github.breadmoirai.bot.framework.event.args.impl;

import com.github.breadmoirai.bot.framework.event.args.ArgumentType;
import com.github.breadmoirai.bot.framework.event.args.CommandArgument;

import java.util.function.Function;
import java.util.function.Predicate;

public class ArgumentTypeImpl<T> implements ArgumentType<T> {
    Predicate<CommandArgument> predicate;
    Function<CommandArgument, T> function;

    public ArgumentTypeImpl(Predicate<CommandArgument> predicate, Function<CommandArgument, T> function) {
        this.predicate = predicate;
        this.function = function;
    }

    @Override
    public T apply(CommandArgument arg) {
        return function.apply(arg);
    }

    @Override
    public boolean test(CommandArgument arg) {
        return predicate.test(arg);
    }
}
