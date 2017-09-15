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

import com.github.breadmoirai.bot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.bot.framework.command.impl.CommandHandle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommandMethodBuilder extends CommandHandleBuilder {

    private final List<CommandParameterBuilder> parameterBuilderList;

    public CommandMethodBuilder(Method method) {
        super(method.getName());
        parameterBuilderList = new ArrayList<>();
        Arrays.stream(method.getParameters())
                .map(CommandParameterBuilder::new)
                .forEachOrdered(parameterBuilderList::add);
    }

    @Override
    public String[] getKeys() {
        return new String[0];
    }

    @Override
    public CommandHandle build() {
        return null;
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
