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
package com.github.breadmoirai.bot.framework.command.arg.impl;

import com.github.breadmoirai.bot.framework.command.arg.ArgumentMapperSimple;
import com.github.breadmoirai.bot.framework.command.arg.CommandArgument;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArgumentTypeSimpleImpl<T> implements ArgumentMapperSimple<T> {
    private final Predicate<CommandArgument> predicate;
    private final Function<CommandArgument, T> mapper;

    public ArgumentTypeSimpleImpl(Predicate<CommandArgument> predicate, Function<CommandArgument, T> mapper) {
        this.predicate = predicate;
        this.mapper = mapper;
    }


    @Override
    public Optional<T> apply(CommandArgument arg) {
        if (predicate.test(arg)) {
            return Optional.of(mapper.apply(arg));
        } else return Optional.empty();
    }
}
