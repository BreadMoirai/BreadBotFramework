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
package com.github.breadmoirai.bot.framework.command.buildernew;

import com.github.breadmoirai.bot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMapBuilder;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandHandleBuilderImpl implements CommandHandleBuilder {

    private String key, name, group, description;
    private final BreadBotClientBuilder builder;
    private final Supplier<Consumer<CommandEvent>> command;
    private final List<CommandHandleBuilder> subCommands;
    private final List<CommandPreprocessor> preprocessors;
    private final CommandPropertyMapBuilder propertyMap;

    public CommandHandleBuilderImpl(BreadBotClientBuilder builder, Supplier<Consumer<CommandEvent>> command) {
        this.builder = builder;
        this.command = command;
        this.subCommands = new ArrayList<>();
        this.preprocessors = new ArrayList<>();
        this.propertyMap = new CommandPropertyMapBuilder();
    }


    @Override
    public CommandHandleBuilder addSubCommand(String key, Supplier<Consumer<CommandEvent>> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        final CommandHandleBuilderImpl commandHandleBuilder = new CommandHandleBuilderImpl(builder, commandSupplier);
        commandHandleBuilder.setKey(key);
        configurator.accept(commandHandleBuilder);
        subCommands.add(commandHandleBuilder);
        return this;
    }

    @Override
    public CommandHandleBuilder setKey(String key) {
        this.key = key;
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
    public CommandPropertyMapBuilder getPropertyMapBuilder() {
        return propertyMap;
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
    public CommandHandle build() {
        return null;
    }
}
