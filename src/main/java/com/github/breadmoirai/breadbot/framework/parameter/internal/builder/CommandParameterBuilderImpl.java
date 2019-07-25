/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.event.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.internal.BreadBotImpl;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.ArgumentParserImpl;
import com.github.breadmoirai.breadbot.framework.parameter.internal.CommandParameterImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CommandParameterBuilderImpl implements CommandParameterBuilder {
//
//    private static final List<Class<?>> COLLECTION_TYPES = Arrays.asList(List.class, Deque.class, Queue.class,
// Stream.class, IntStream.class, LongStream.class, DoubleStream.class);

    private final CommandHandleBuilder commandBuilder;
    private final Parameter parameter;
    private final CommandPropertyMapImpl map;
    private final BreadBotBuilder clientBuilder;
    private String paramName;
    private int index = 0;
    private int width = 1;
    private int limit = -1;
    private TypeParser<?> typeParser;
    private Function<CommandParameterBuilderImpl, ArgumentParser> argumentParser;
    private boolean mustBePresent = false;
    private boolean contiguous = false;
    private AbsentArgumentHandler absentArgumentHandler = null;
    private Predicate<CommandArgument> argumentPredicate;
    private Function<CommandEvent, ?> defaultValue;

    public CommandParameterBuilderImpl(BreadBotBuilder builder, CommandHandleBuilder commandBuilder,
                                       Parameter parameter, CommandPropertyMapImpl map) {
        this.commandBuilder = commandBuilder;
        this.parameter = parameter;
        this.clientBuilder = builder;
        if (parameter != null) {
            this.map = new CommandPropertyMapImpl(map, parameter.getAnnotations());
            this.paramName = parameter.getName();
            final Class<?> type = parameter.getType();
            typeParser = this.clientBuilder.getTypeParser(type);
            if (CommandPlugin.class.isAssignableFrom(type)) {
                this.argumentParser = (p) -> new ArgumentParser() {
                    private CommandPlugin p;

                    @Override
                    public Object parse(CommandParameter param, CommandArgumentList list, CommandParser parser) {
                        if (p == null)
                            p = ((BreadBotImpl) parser.getEvent().getClient()).getPlugin(type);
                        return p;
                    }
                };
            } else {
                this.argumentParser = (p) -> new ArgumentParserImpl(p.index, p.width, p.mustBePresent,
                                                                    p.absentArgumentHandler, p.typeParser,
                                                                    p.defaultValue);
            }
            builder.applyTypeModifiers(this);
        } else {
            this.map = null;
        }

        builder.applyModifiers(this);
    }

    private static Class<?> getActualTypeParameter(Parameter parameter) {
        final ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        final Type type = parameterizedType.getActualTypeArguments()[0];
        return ((Class<?>) type);
    }

    public Class<?> getGenericType() {
        return getActualTypeParameter(getDeclaringParameter());
    }

    @Override
    public CommandParameterBuilder setName(String paramName) {
        this.paramName = paramName;
        return this;
    }

    @Override
    public <T> CommandParameterBuilder setTypeParser(TypeParser<T> parser) {
        this.typeParser = parser;
        return this;
    }

    @Override
    public CommandParameterBuilder setParser(ArgumentParser parser) {
        this.argumentParser = o -> parser;
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

    @Override
    public CommandParameterBuilder addArgumentPredicate(Predicate<CommandArgument> argumentPredicate) {
        if (this.argumentPredicate == null) {
            this.argumentPredicate = argumentPredicate;
        } else {
            this.argumentPredicate = this.argumentPredicate.and(argumentPredicate);
        }
        return this;
    }

    public TypeParser<?> getTypeParser() {
        return typeParser;
    }

    public void setArgumentParser(Function<CommandParameterBuilderImpl, ArgumentParser> p) {
        this.argumentParser = p;
    }

    @Override
    public CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    @Override
    public BreadBotBuilder getClientBuilder() {
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
        return map != null ? map.getProperty(propertyType) : null;
    }

    @Override
    public boolean hasProperty(Class<?> propertyType) {
        return map != null && map.hasProperty(propertyType);
    }

    @Override
    public CommandParameter build() {
        if (argumentPredicate != null && typeParser != null) {
            final TypeParser<?> typeParser = this.typeParser;
            final Predicate<CommandArgument> predicate = this.argumentPredicate;
            this.typeParser = arg -> predicate.test(arg) ? typeParser.parse(arg) : null;
        }
        final ArgumentParser parser = this.argumentParser.apply(this);

        return new CommandParameterImpl(paramName, parameter, index, width, limit, contiguous, parser, mustBePresent,
                                        absentArgumentHandler);
    }

    @Override
    public CommandParameterBuilder setDefaultValue(Function<CommandEvent, ?> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public CommandPropertyMapImpl getMap() {
        return map;
    }

    public String getParamName() {
        return paramName;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public CommandParameterBuilderImpl setIndex(int index) {
        this.index = index;
        return this;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public CommandParameterBuilderImpl setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public CommandParameterBuilderImpl setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public boolean isMustBePresent() {
        return mustBePresent;
    }

    public boolean isContiguous() {
        return contiguous;
    }

    public AbsentArgumentHandler getAbsentArgumentHandler() {
        return absentArgumentHandler;
    }

    public Predicate<CommandArgument> getArgumentPredicate() {
        return argumentPredicate;
    }

    public Function<CommandEvent, ?> getDefaultValue() {
        return defaultValue;
    }

}
