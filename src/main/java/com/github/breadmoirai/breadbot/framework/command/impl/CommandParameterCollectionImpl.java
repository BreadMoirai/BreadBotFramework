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
package com.github.breadmoirai.breadbot.framework.command.impl;

import com.github.breadmoirai.breadbot.framework.command.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParser;

import java.util.stream.Collector;
import java.util.stream.Stream;

public class CommandParameterCollectionImpl implements CommandParameter {
    private final CommandParameterImpl commandParameter;
    private final Collector<Object, Object, Object> collector;

    public CommandParameterCollectionImpl(CommandParameterImpl commandParameter, Collector<Object, Object, Object> collector) {
        this.commandParameter = commandParameter;
        this.collector = collector;
    }

    @Override
    public Object map(CommandArgumentList list, CommandParser set) {
        final Stream.Builder<Object> builder = Stream.builder();
        Object o;
        while ((o = commandParameter.map(list, set)) != null) {
            builder.accept(o);
        }
        return builder.build().collect(collector);
    }

    @Override
    public Class<?> getType() {
        return commandParameter.getType();
    }

    @Override
    public int getFlags() {
        return commandParameter.getFlags();
    }

    @Override
    public int getIndex() {
        return commandParameter.getIndex();
    }

    @Override
    public int getWidth() {
        return commandParameter.getWidth();
    }

    @Override
    public boolean isMustBePresent() {
        return commandParameter.isMustBePresent();
    }
}
