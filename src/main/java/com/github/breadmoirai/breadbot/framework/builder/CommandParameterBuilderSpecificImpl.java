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

import com.github.breadmoirai.breadbot.framework.command.parameter.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandParameterBuilderSpecificImpl implements CommandParameterBuilder {

    private Parameter parameter;
    private final Supplier<CommandParameter> supplier;
    private final String error;

    public CommandParameterBuilderSpecificImpl(Parameter parameter, String error, Supplier<CommandParameter> supplier) {
        this.parameter = parameter;
        this.supplier = supplier;
        this.error = error;
    }

    @Override
    public Parameter getDeclaringParameter() {
        return parameter;
    }

    @Override
    public CommandParameterBuilder setName(String paramName) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public CommandParameterBuilder setFlags(int flags) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public CommandParameterBuilder setIndex(int index) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public CommandParameterBuilder setWidth(int width) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public <T> CommandParameterBuilder setBaseType(Class<T> type) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentTypeMapper<T> mapper) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public <T> CommandParameterBuilder setParser(@Nullable ArgumentTypePredicate predicate, ArgumentTypeMapper<T> parser) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public CommandParameterBuilder setRequired(boolean mustBePresent) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public CommandParameterBuilder setOnParamNotFound(MissingArgumentConsumer onParamNotFound) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public ArgumentParser<?> getParser() {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public <T> T getProperty(Class<T> propertyType) {
        return null;
    }

    @Override
    public boolean hasProperty(Class<?> propertyType) {
        return false;
    }

    @Override
    public CommandParameter build() {
        return supplier.get();
    }

}
