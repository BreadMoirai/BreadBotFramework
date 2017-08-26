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

import com.github.breadmoirai.bot.framework.event.args.ArgumentMapper;
import com.github.breadmoirai.bot.framework.event.args.ArgumentTypeMapper;
import com.github.breadmoirai.bot.framework.event.args.ArgumentTypePredicate;
import com.github.breadmoirai.bot.framework.event.args.CommandArgument;

import java.util.Optional;

public class ArgumentTypeImpl<T> implements ArgumentMapper<T> {
    private final ArgumentTypePredicate predicate;
    private final ArgumentTypeMapper<T> mapper;

    public ArgumentTypeImpl(ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        this.predicate = predicate;
        this.mapper = mapper;
    }

    @Override
    public Optional<T> map(CommandArgument arg, int flags) {
        if (predicate.test(arg, flags)) {
            return Optional.of(mapper.map(arg, flags));
        } else return Optional.empty();
    }
}
