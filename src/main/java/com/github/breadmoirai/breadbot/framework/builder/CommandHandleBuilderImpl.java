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

import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.impl.CommandHandleImpl;
import com.github.breadmoirai.breadbot.framework.command.impl.CommandObjectFactory;
import com.github.breadmoirai.breadbot.framework.command.impl.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.command.impl.InvokableCommand;
import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class CommandHandleBuilderImpl extends CommandHandleBuilderInternal {

    private final Object declaringObject;
    private final Class<?> declaringClass;
    private final Method declaringMethod;
    private String[] keys;
    private String name, group, description;
    private final BreadBotClientBuilder builder;
    private final CommandObjectFactory commandFactory;
    private final CommandParameterBuilder[] parameterBuilders;
    private final InvokableCommand commandFunction;
    private final List<CommandHandleBuilderInternal> subCommands;
    private final List<CommandPreprocessor> preprocessors;
    private final CommandPropertyMapImpl propertyMap;

    public CommandHandleBuilderImpl(Object declaringObject,
                                    Class<?> declaringClass,
                                    Method declaringMethod,
                                    BreadBotClientBuilder builder,
                                    CommandObjectFactory commandFactory,
                                    CommandParameterBuilder[] parameterBuilders,
                                    InvokableCommand commandFunction,
                                    CommandPropertyMapImpl propertyMap) {
        this.declaringObject = declaringObject;
        this.declaringClass = declaringClass;
        this.declaringMethod = declaringMethod;
        this.builder = builder;
        this.commandFactory = commandFactory;
        this.parameterBuilders = parameterBuilders;
        this.commandFunction = commandFunction;
        this.subCommands = new ArrayList<>();
        this.preprocessors = new ArrayList<>();
        this.propertyMap = propertyMap == null ? new CommandPropertyMapImpl() : propertyMap;
    }

    @Override
    public CommandHandleBuilder createSubCommand(Consumer<CommandEvent> onCommand) {
        CommandHandleBuilderInternal handleBuilder = new CommandHandleBuilderFactoryImpl(getClientBuilder()).createCommand(onCommand);
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
    public List<CommandPreprocessor> getPreprocessors() {
        return preprocessors;
    }

    @Override
    public CommandHandleBuilder sortPreprocessors(Comparator<CommandPreprocessor> preprocessorComparator) {
        preprocessors.sort(preprocessorComparator);
        return null;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public Method getDeclaringMethod() {
        return declaringMethod;
    }

    @Override
    public Object getDeclaringObject() {
        return declaringObject;
    }

    @Override
    public BreadBotClientBuilder getClientBuilder() {
        return builder;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<CommandHandleBuilder> getSubCommands() {
        return Collections.unmodifiableList(subCommands);
    }

    @Override
    public CommandHandle build() {
        Map<String, CommandHandle> subCommandMap;
        if (subCommands.isEmpty()) {
            subCommandMap = null;
        } else {
            subCommandMap = new HashMap<>();
            for (CommandHandleBuilderInternal subCommand : subCommands) {
                CommandHandle command = subCommand.build();
                for (String key : command.getKeys()) {
                    subCommandMap.put(key, command);
                }
            }
        }
        final CommandParameter[] commandParameters = new CommandParameter[parameterBuilders.length];
        Arrays.setAll(commandParameters, value -> parameterBuilders[value].build());
        return new CommandHandleImpl(keys, name, group, description, /*client,*/ commandFactory, commandParameters, commandFunction, subCommandMap, preprocessors, propertyMap);
    }
}
