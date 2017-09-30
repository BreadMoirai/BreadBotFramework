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

import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.bot.framework.command.impl.FunctionalCommandImpl;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public class FunctionalCommandBuilder extends CommandHandleBuilder {

    private final Consumer<CommandEvent> function;

    public FunctionalCommandBuilder(String name, Consumer<CommandEvent> function) {
        super(name, null, new Annotation[0]);
        this.function = function;
    }

    @Override
    public CommandHandle build() {
        return new FunctionalCommandImpl(getName(), getKeys(), getPreprocessorList(), getPropertyBuilder().build(), function);
    }

    @Override
    public FunctionalCommandBuilder setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public FunctionalCommandBuilder setKeys(String... keys) {
        super.setKeys(keys);
        return this;
    }

    @Override
    public FunctionalCommandBuilder putProperty(Object property) {
        super.putProperty(property);
        return this;
    }

    @Override
    public <T> FunctionalCommandBuilder putProperty(Class<? super T> type, T property) {
        super.putProperty(type, property);
        return this;
    }

    @Override
    public FunctionalCommandBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        super.addPreprocessorFunction(identifier, function);
        return this;
    }

    @Override
    public FunctionalCommandBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        super.addPreprocessorPredicate(identifier, predicate);
        return this;
    }

    @Override
    public FunctionalCommandBuilder addPreprocessors(Iterable<CommandPreprocessor> preprocessors) {
        super.addPreprocessors(preprocessors);
        return this;
    }

    @Override
    public FunctionalCommandBuilder sortPreprocessors() {
        super.sortPreprocessors();
        return this;
    }

    @Override
    public FunctionalCommandBuilder addAssociatedPreprocessors() {
        super.addAssociatedPreprocessors();
        return this;
    }
}
