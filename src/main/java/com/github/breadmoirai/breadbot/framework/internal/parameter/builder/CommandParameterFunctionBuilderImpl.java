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

import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.internal.parameter.CommandParameterFunctionImpl;
import com.github.breadmoirai.breadbot.framework.parameter.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandParameterFunctionBuilderImpl implements CommandParameterBuilder {

    private final BreadBotClientBuilder clientBuilder;
    private final CommandHandleBuilder handleBuilder;
    private Parameter parameter;
    private final Function<CommandParser, ?> function;
    private final String error;
    private String name;

    public CommandParameterFunctionBuilderImpl(BreadBotClientBuilder clientBuilder, CommandHandleBuilder handleBuilder, Parameter parameter, String error, Function<CommandParser, ?> function) {

        this.clientBuilder = clientBuilder;
        this.handleBuilder = handleBuilder;
        this.parameter = parameter;
        this.error = error;
        this.function = function;
    }

    @Override
    public BreadBotClientBuilder getClientBuilder() {
        return clientBuilder;
    }

    @Override
    public CommandHandleBuilder getCommandBuilder() {
        return handleBuilder;
    }

    @Override
    public Parameter getDeclaringParameter() {
        return parameter;
    }

    @Override
    public CommandParameterBuilder setName(String paramName) {
        this.name = paramName;
        return this;
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
    public CommandParameterBuilder setOnAbsentArgument(AbsentArgumentHandler onParamNotFound) {
        throw new UnsupportedOperationException(error);
    }

    @Override
    public CommandParameterBuilder setContiguous(boolean isContiguous) {
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
        return new CommandParameterFunctionImpl(name, parameter, function);
    }

}