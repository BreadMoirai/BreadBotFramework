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
package com.github.breadmoirai.bot.framework.command.builder;

import com.github.breadmoirai.bot.framework.command.*;
import com.github.breadmoirai.bot.framework.command.buildernew.CommandParameterBuilder;
import com.github.breadmoirai.bot.framework.command.impl.CommandMethodImpl;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessorFunction;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessorPredicate;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommandMethodBuilder extends CommandHandleBuilder {

    private final List<CommandParameterBuilder> parameterBuilderList;
    private final Method method;

    public CommandMethodBuilder(Method method, CommandPropertyMap map) {
        super(method.getName(), map, method.getAnnotations());
        this.method = method;
        parameterBuilderList = new ArrayList<>();
        final CommandParameterBuilder.Factory factory = new CommandParameterBuilder.Factory(map, method.getName());
        Arrays.stream(method.getParameters())
                .map(factory::builder)
                .forEachOrdered(parameterBuilderList::add);
        final Command property = getPropertyBuilder().getProperty(Command.class);
        if (property != null) {
            final String[] value = property.value();
            if (value.length != 0) {
                setKeys(value);
            }
        }
        if (getKeys() == null || getKeys().length == 0)
            setKeys(method.getName().toLowerCase());
    }

    @Override
    public CommandHandle build() {
        List<CommandParameter> list = new ArrayList<>();
        for (CommandParameterBuilder commandParameterBuilder : parameterBuilderList) {
            CommandParameter build = commandParameterBuilder.build();
            list.add(build);
        }
        try {
            return new CommandMethodImpl(method, list.toArray(new CommandParameter[0]), getPropertyBuilder().build(), getPreprocessorList(), getKeys());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public CommandMethodBuilder configure(Consumer<CommandMethodBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    public CommandMethodBuilder configureParameter(int paramIndex, Consumer<CommandParameterBuilder> configurator) {
        parameterBuilderList.get(paramIndex).configure(configurator);
        return this;
    }

    @Override
    public CommandMethodBuilder setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public CommandMethodBuilder setKeys(String... keys) {
        super.setKeys(keys);
        return this;
    }

    @Override
    public CommandMethodBuilder putProperty(Object property) {
        super.putProperty(property);
        return this;
    }

    @Override
    public <T> CommandMethodBuilder putProperty(Class<? super T> type, T property) {
        super.putProperty(type, property);
        return this;
    }

    @Override
    public CommandMethodBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        super.addPreprocessorFunction(identifier, function);
        return this;
    }

    @Override
    public CommandMethodBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        super.addPreprocessorPredicate(identifier, predicate);
        return this;
    }

    @Override
    public CommandMethodBuilder addPreprocessors(Iterable<CommandPreprocessor> preprocessors) {
        super.addPreprocessors(preprocessors);
        return this;
    }

    @Override
    public CommandMethodBuilder sortPreprocessors() {
        super.sortPreprocessors();
        return this;
    }

    @Override
    public CommandMethodBuilder addAssociatedPreprocessors() {
        super.addAssociatedPreprocessors();
        return this;
    }
}
