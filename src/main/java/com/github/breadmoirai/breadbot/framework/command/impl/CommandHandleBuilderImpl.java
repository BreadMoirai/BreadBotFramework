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

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.command.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.*;
import java.util.function.Consumer;

public class CommandHandleBuilderImpl implements CommandHandleBuilder {

    private String[] keys;
    private String name, group, description;
    private final Object declaringObject;
    private final BreadBotClientBuilder builder;
    private final CommandObjectFactory commandFactory;
    private final CommandParameterBuilder[] parameterBuilders;
    private final InvokableCommand commandFunction;
    private final List<CommandHandleBuilder> subCommands;
    private final List<CommandPreprocessor> preprocessors;
    private final CommandPropertyMapImpl propertyMap;

    public CommandHandleBuilderImpl(Object declaringObject,
                                    BreadBotClientBuilder builder,
                                    CommandObjectFactory commandFactory,
                                    CommandParameterBuilder[] parameterBuilders,
                                    InvokableCommand commandFunction) {
        this.declaringObject = declaringObject;
        this.builder = builder;
        this.commandFactory = commandFactory;
        this.parameterBuilders = parameterBuilders;
        this.commandFunction = commandFunction;
        this.subCommands = new ArrayList<>();
        this.preprocessors = new ArrayList<>();
        this.propertyMap = new CommandPropertyMapImpl();
    }

    public CommandHandleBuilderImpl(Object declaringObject,
                                    BreadBotClientBuilder builder,
                                    CommandObjectFactory commandFactory,
                                    CommandParameterBuilder[] parameterBuilders,
                                    InvokableCommand commandFunction,
                                    CommandPropertyMapImpl propertyMap) {
        this.declaringObject = declaringObject;
        this.builder = builder;
        this.commandFactory = commandFactory;
        this.parameterBuilders = parameterBuilders;
        this.commandFunction = commandFunction;
        this.subCommands = new ArrayList<>();
        this.preprocessors = new ArrayList<>();
        this.propertyMap = propertyMap;
    }

    @Override
    public CommandHandleBuilder createSubCommand(Consumer<CommandEvent> onCommand) {
        CommandHandleBuilder handleBuilder = new CommandHandleBuilderFactory(getClientBuilder()).fromConsumer(onCommand);
        addSubCommand(handleBuilder);
        return handleBuilder;
    }

    @Override
    public boolean containsProperty(Class<?> propertyType) {
        return propertyMap.containsProperty(propertyType);
    }

    @Override
    public <T> T getProperty(Class<T> propertyType) {
        return propertyMap.getProperty(propertyType);
    }

    void addSubCommand(CommandHandleBuilder subCommandBuilder) {
        subCommands.add(subCommandBuilder);
    }

    @Override
    public CommandHandleBuilder setKeys(String... keys) {
        this.keys = keys;
        return this;
    }

    @Override
    public CommandHandleBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public CommandHandleBuilder setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public CommandHandleBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public <T> CommandHandleBuilder putProperty(Class<? super T> type, T property) {
        propertyMap.putProperty(type, property);
        return this;
    }

    @Override
    public CommandHandleBuilder putProperty(Object property) {
        propertyMap.putProperty(property);
        return this;
    }

    @Override
    public CommandHandleBuilder addPreprocessor(CommandPreprocessor preprocessor) {
        preprocessors.add(preprocessor);
        return this;
    }

    @Override
    public CommandHandleBuilder sortPreprocessors(Comparator<CommandPreprocessor> preprocessorComparator) {
        preprocessors.sort(preprocessorComparator);
        return null;
    }

    @Override
    public BreadBotClientBuilder getClientBuilder() {
        return builder;
    }

    @Override
    public Object getDeclaringObject() {
        return declaringObject;
    }

    @Override
    public CommandHandle build(BreadBotClient client) {
        Map<String, CommandHandle> subCommandMap;
        if (subCommands.isEmpty()) {
            subCommandMap = null;
        } else {
            subCommandMap = new HashMap<>();
            for (CommandHandleBuilder subCommand : subCommands) {
                CommandHandle command = subCommand.build(client);
                for (String key : command.getKeys()) {
                    subCommandMap.put(key, command);
                }
            }
        }
        final CommandParameter[] commandParameters = new CommandParameter[parameterBuilders.length];
        Arrays.setAll(commandParameters, value -> parameterBuilders[value].build());
        return new CommandHandleImpl(keys, name, group, description, client, commandFactory, commandParameters, commandFunction, subCommandMap, preprocessors, propertyMap);
    }
}
