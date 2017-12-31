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

package com.github.breadmoirai.breadbot.framework.parameter.internal.builder;

import com.github.breadmoirai.breadbot.framework.CommandModule;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.lang.reflect.Parameter;

public class CommandParameterBuilderFactory {
    private final BreadBotClientBuilder clientBuilder;
    private final CommandHandleBuilder handleBuilder;
    private final CommandPropertyMapImpl map;

    public CommandParameterBuilderFactory(BreadBotClientBuilder clientBuilder, CommandHandleBuilder handleBuilder, CommandPropertyMapImpl map) {
        this.clientBuilder = clientBuilder;
        this.handleBuilder = handleBuilder;
        this.map = map;
    }

    public CommandParameterBuilder builder(Parameter parameter) {
        final Class<?> type = parameter.getType();
        if (type == CommandEvent.class) {
            return new CommandParameterBuilderImpl(clientBuilder, handleBuilder, parameter, map).setParser((parameter1, list, parser) -> parser.getEvent());
        } else if (CommandModule.class.isAssignableFrom(type)) {
            return new CommandParameterBuilderImpl(clientBuilder, handleBuilder, parameter, map).setParser((parameter1, list, parser) -> parser.getEvent().getClient().getModule(type));
        } else {
            CommandParameterBuilderImpl param = new CommandParameterBuilderImpl(clientBuilder, handleBuilder, parameter, map);
            clientBuilder.applyModifiers(param);
            return param;
        }
    }
}