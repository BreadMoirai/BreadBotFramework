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
package com.github.breadmoirai.bot.framework.command.impl;

import com.github.breadmoirai.bot.framework.ICommandModule;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParameterFunctionImpl;
import com.github.breadmoirai.bot.framework.command.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.lang.reflect.Parameter;

public class CommandParameterBuilderFactory {
    private final CommandPropertyMap map;
    private final String methodName;

    public CommandParameterBuilderFactory(CommandPropertyMap map, String methodName) {
        this.map = map;
        this.methodName = methodName;
    }

    public CommandParameterBuilder builder(Parameter parameter) {
        final Class<?> type = parameter.getType();
        if (type == CommandEvent.class) {
            return new CommandParameterBuilderSpecificImpl("This parameter of type CommandEvent is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent()));
        } else if (ICommandModule.class.isAssignableFrom(type)) {
            return new CommandParameterBuilderSpecificImpl("This parameter of type " + type.getSimpleName() + " is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent().getClient().getModule(type)));
        } else {
            return new CommandParameterBuilderImpl(parameter, methodName, map);
        }
    }
}
