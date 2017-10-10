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

import com.github.breadmoirai.bot.framework.BreadBotClient;
import com.github.breadmoirai.bot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMapBuilder;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandHandleBuilderImpl implements CommandHandleBuilder {

    private String[] keys;
    private String name, group, description;
    private final BreadBotClientBuilder builder;
    private final Function<Object, Object> commandSupplier;
    private final BiConsumer<Object, CommandEvent> commandFunction;
    private final List<CommandHandleBuilder> subCommands;
    private final List<CommandPreprocessor> preprocessors;
    private final CommandPropertyMapBuilder propertyMap;

    public CommandHandleBuilderImpl(BreadBotClientBuilder builder, Function<Object, Object> commandSupplier, BiConsumer<Object, CommandEvent> commandFunction) {
        this.builder = builder;
        this.commandSupplier = commandSupplier;
        this.commandFunction = commandFunction;
        this.subCommands = new ArrayList<>();
        this.preprocessors = new ArrayList<>();
        this.propertyMap = new CommandPropertyMapBuilder();
    }

    @Override
    public CommandHandleBuilder addSubCommand(String[] keys, Consumer<CommandEvent> command, Consumer<CommandHandleBuilder> configurator) {
        @SuppressWarnings("unchecked") BiConsumer<Object, CommandEvent> biConsumer = (o, commandEvent) -> ((Consumer<CommandEvent>) o).accept(commandEvent);
        final CommandHandleBuilderImpl commandHandleBuilder = new CommandHandleBuilderImpl(builder, o -> command, biConsumer);
        commandHandleBuilder.setKeys(keys);
        configurator.accept(commandHandleBuilder);
        subCommands.add(commandHandleBuilder);
        return this;
    }

    CommandHandleBuilder addSubCommand(String key,
                                       Function<Object, Object> commandSupplier,
                                       BiConsumer<Object, CommandEvent> commandFunction,
                                       Consumer<CommandHandleBuilder> configurator) {

        final CommandHandleBuilderImpl commandHandleBuilder = new CommandHandleBuilderImpl(builder, commandSupplier, commandFunction);
        commandHandleBuilder.setKeys(key);
        configurator.accept(commandHandleBuilder);
        subCommands.add(commandHandleBuilder);
        return this;
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
    public CommandHandle build(BreadBotClient client) {
        Map<String, CommandHandle> subCommandMap = new HashMap<>();
        for (CommandHandleBuilder subCommand : subCommands) {
            CommandHandle command = subCommand.build(client);
            for (String key : command.getKeys()) {
                subCommandMap.put(key, command);
            }
        }

        return new CommandHandleImpl(keys, name, group, description, client, commandSupplier, commandParameters, commandFunction, subCommandMap, preprocessors, propertyMap.build());
    }
}
