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
package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.ICommandModule;
import com.github.breadmoirai.breadbot.framework.command.CommandPropertyMap;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParameterFunctionImpl;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.lang.reflect.Parameter;

public class CommandParameterBuilderFactory {
    private BreadBotClientBuilder clientBuilder;
    private final CommandPropertyMap map;
    private final String methodName;

    public CommandParameterBuilderFactory(BreadBotClientBuilder clientBuilder, CommandPropertyMap map, String methodName) {
        this.clientBuilder = clientBuilder;
        this.map = map;
        this.methodName = methodName;
    }

    public CommandParameterBuilder builder(Parameter parameter) {
        final Class<?> type = parameter.getType();
        if (type == CommandEvent.class) {
            return new CommandParameterBuilderSpecificImpl(parameter, "This parameter of type CommandEvent is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent()));
        } else if (ICommandModule.class.isAssignableFrom(type)) {
            return new CommandParameterBuilderSpecificImpl(parameter,"This parameter of type " + type.getSimpleName() + " is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent().getClient().getModule(type)));
        } else {
            CommandParameterBuilderImpl param = new CommandParameterBuilderImpl(parameter, methodName, map);
            clientBuilder.getCommandProperties().applyModifiers(param);
            return param;
        }
    }
}
