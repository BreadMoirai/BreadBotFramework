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

import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.ArgumentParserImpl;
import com.github.breadmoirai.breadbot.framework.parameter.internal.CommandParameterImpl;
import com.github.breadmoirai.breadbot.util.TypeFinder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class CommandParameterBuilderImpl implements CommandParameterBuilder {

    private static final List<Class<?>> COLLECTION_TYPES = Arrays.asList(List.class, Deque.class, Queue.class, Stream.class, IntStream.class, LongStream.class, DoubleStream.class);

    private final CommandHandleBuilder commandBuilder;
    private final Parameter parameter;
    private final CommandPropertyMapImpl map;
    private final BreadBotClientBuilder clientBuilder;
    private String paramName;
    private int index = 0, width = 1;
    private TypeParser<?> typeParser;
    private ArgumentParser argumentParser;
    private boolean mustBePresent = false;
    private boolean contiguous = false;
    private AbsentArgumentHandler absentArgumentHandler = null;

    public CommandParameterBuilderImpl(BreadBotClientBuilder builder, CommandHandleBuilder commandBuilder, Parameter parameter, CommandPropertyMapImpl map) {
        this.commandBuilder = commandBuilder;
        this.parameter = parameter;
        this.clientBuilder = builder;
        if (parameter != null) {
            this.map = new CommandPropertyMapImpl(map, parameter.getAnnotations());
            this.paramName = parameter.getName();
        } else {
            this.map = null;
        }

        builder.applyModifiers(this);
    }

    public Class<?> getParameterType() {
        return getDeclaringParameter().getType();
    }

    public Class<?> getGenericType() {
        return getActualTypeParameter(getDeclaringParameter());
    }

    private static Class<?> getActualTypeParameter(Parameter parameter) {
        final ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        final Type type = parameterizedType.getActualTypeArguments()[0];
        return ((Class<?>) type);
    }

    private boolean isCollection() {
        final Class<?> paramType = parameter.getType();
        return COLLECTION_TYPES.stream().anyMatch(a -> a == paramType);
    }

    private void setCollectionParser() {
        Class<?> paramType = parameter.getType();
        if (paramType == List.class) {
            Class<?> type = getGenericType();
            CollectionParserFactory.setParserToGenericList(type, this);
        } else if (paramType == Deque.class || paramType == Queue.class) {
            Class<?> type = getGenericType();
            CollectionParserFactory.setParserToGenericDeque(type, this);
        } else if (paramType == Stream.class) {
            Class<?> type = getGenericType();
            CollectionParserFactory.setParserToGenericStream(type, this);
        } else if (paramType == IntStream.class) {
            CollectionParserFactory.setParserToIntStream(this);
        } else {
            this.typeParser = this.clientBuilder.getTypeParser(paramType);
            this.argumentParser = new ArgumentParserImpl();
        }
    }

    @Override
    public CommandParameterBuilder setName(String paramName) {
        this.paramName = paramName;
        return this;
    }

    @Override
    public CommandParameterBuilderImpl setIndex(int index) {
        this.index = index;
        return this;
    }

    @Override
    public CommandParameterBuilderImpl setWidth(int width) {
        this.width = width;
        return this;
    }

    @Override
    public <T> CommandParameterBuilder setTypeParser(TypeParser<T> parser) {
        final Class<?> aClass = (Class<?>) TypeFinder.getTypeArguments(parser.getClass(), TypeParser.class)[0];
        if (isCollection()) {
            final Class<?> genericType = getGenericType();
            if (aClass != genericType) {
                throw new IllegalArgumentException(String.format("The provided TypeParser has result type [%s] which conflicts with the generic parameter type [%s]", aClass.getSimpleName(), genericType.getSimpleName()));
            }
        } else {
            if (aClass != getParameterType()) {
                throw new IllegalArgumentException(String.format("The provided TypeParser has result type [%s] which conflicts with the parameter type [%s]", aClass.getSimpleName(), getParameterType().getSimpleName()));
            }
        }
        this.typeParser = parser;
        return this;
    }

    @Override
    public CommandParameterBuilder setParser(ArgumentParser parser) {
        this.argumentParser = parser;
        return this;
    }


    @Override
    public CommandParameterBuilder setRequired(boolean mustBePresent) {
        this.mustBePresent = mustBePresent;
        return this;
    }

    @Override
    public CommandParameterBuilder setOnAbsentArgument(AbsentArgumentHandler onParamNotFound) {
        this.absentArgumentHandler = onParamNotFound;
        return this;
    }

    @Override
    public CommandParameterBuilder setContiguous(boolean isContiguous) {
        this.contiguous = isContiguous;
        return this;
    }

    public TypeParser<?> getTypeParser() {
        return typeParser;
    }

    public ArgumentParser getParser() {
        return argumentParser;
    }

    @Override
    public CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    @Override
    public BreadBotClientBuilder getClientBuilder() {
        return clientBuilder;
    }

    @Override
    public CommandHandleBuilder getCommandBuilder() {
        return commandBuilder;
    }

    @Override
    public Parameter getDeclaringParameter() {
        return parameter;
    }

    @Override
    public Method getDeclaringMethod() {
        return getCommandBuilder().getDeclaringMethod();
    }

    @Override
    public <T> T getProperty(Class<T> propertyType) {
        return map.getProperty(propertyType);
    }

    @Override
    public boolean hasProperty(Class<?> propertyType) {
        return map.hasProperty(propertyType);
    }

    @Override
    public CommandParameter build() {
        if (typeParser == null && argumentParser == null) {
            setCollectionParser();
        }
        final CommandParameterImpl commandParameter = new CommandParameterImpl(paramName, parameter, index, width, contiguous, typeParser, argumentParser, mustBePresent, absentArgumentHandler);

        return commandParameter;
    }
}
