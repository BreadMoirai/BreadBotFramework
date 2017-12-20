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

package com.github.breadmoirai.breadbot.framework.internal.parameter.builder;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.CommandModule;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;

import java.lang.reflect.Parameter;

public class CommandParameterBuilderFactory {
    private final BreadBotClientBuilder clientBuilder;
    private final CommandHandleBuilder handleBuilder;
    private final CommandPropertyMapImpl map;
    private final String methodName;

    public CommandParameterBuilderFactory(BreadBotClientBuilder clientBuilder, CommandHandleBuilder handleBuilder, CommandPropertyMapImpl map, String methodName) {
        this.clientBuilder = clientBuilder;
        this.handleBuilder = handleBuilder;
        this.map = map;
        this.methodName = methodName;
    }

    public CommandParameterBuilder builder(Parameter parameter) {
        final Class<?> type = parameter.getType();
        if (type == CommandEvent.class) {
            return new CommandParameterFunctionBuilderImpl(clientBuilder, handleBuilder, parameter, "This parameter of type CommandEvent is inconfigurable", CommandParser::getEvent);
        } else if (CommandModule.class.isAssignableFrom(type)) {
            return new CommandParameterFunctionBuilderImpl(clientBuilder, handleBuilder, parameter, "This parameter of type " + type.getSimpleName() + " is inconfigurable", (commandParser) -> commandParser.getEvent().getClient().getModule(type));
        } else {
            CommandParameterBuilderImpl param = new CommandParameterBuilderImpl(clientBuilder, handleBuilder, parameter, methodName, map);
            clientBuilder.applyModifiers(param);
            return param;
        }
    }
}