package com.github.breadmoirai.bot.framework.command.builder;

import com.github.breadmoirai.bot.framework.command.CommandParameter;
import com.github.breadmoirai.bot.framework.command.impl.CommandParameterCollectionImpl;
import com.github.breadmoirai.bot.framework.command.impl.CommandParameterImpl;
import com.github.breadmoirai.bot.framework.command.parser.*;
import com.github.breadmoirai.bot.framework.error.MissingTypeMapperException;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandParameterBuilderImpl implements CommandParameterBuilder {
    private String methodName, paramName;
    private final Class<?> paramType;
    private Function<Class<?>, Collector<?, ?, ?>> collectorSupplier;
    private int flags = 0, index = -1, width = 1;
    private Class<?> type;
    private ArgumentParser<?> parser;
    private boolean mustBePresent = false;
    private boolean contiguous = true;
    private BiConsumer<CommandEvent, CommandParameter> onParamNotFound = null;

    public CommandParameterBuilderImpl(Parameter parameter) {
        paramName = parameter.getName();
        paramType = parameter.getType();
        if (paramType == List.class) {
            collectorSupplier = getToList();
            setActualTypeParameter(parameter);
        } else if (paramType == Stream.class) {
            collectorSupplier = getToStream();
            setActualTypeParameter(parameter);
//        } else if (paramType == Optional.class) {
//            setActualTypeParameter(parameter);
        } else {
            type = paramType;
        }
        parser = ArgumentTypes.getParser(type);
    }

    private void setActualTypeParameter(Parameter parameter) {
        final ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        this.type = (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }

    private <T> Function<Class<?>, Collector<?, ?, ?>> getToList() {
        final Function<Class<T>, Collector<T, ?, List<T>>> f = tClass -> Collectors.toList();
        @SuppressWarnings("unchecked") final Function<Class<?>, Collector<?, ?, ?>> f2 = (Function<Class<?>, Collector<?, ?, ?>>) (Function<?, ?>) f;
        return f2;
    }

    private <T> Function<Class<?>, Collector<?, ?, ?>> getToStream() {
        final Function<Class<T>, Collector<T, Stream.Builder<T>, Stream<T>>> f = tClass -> Collector.of(Stream::builder, Stream.Builder::accept, (tBuilder, tBuilder2) -> {
            tBuilder2.build().forEach(tBuilder);
            return tBuilder;
        }, Stream.Builder::build);
        @SuppressWarnings("unchecked") final Function<Class<?>, Collector<?, ?, ?>> f2 = (Function<Class<?>, Collector<?, ?, ?>>) (Function<?, ?>) f;
        return f2;
    }

    CommandParameterBuilder setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    @Override
    public CommandParameterBuilder setName(String paramName) {
        this.paramName = paramName;
        return this;
    }

//    /**
//     * Sets the actual type of the parameter. This also determines the search criteria for this parameter. If this is not set, it will be the same as the base type.
//     * The argument passed should be of one of the following types
//     * <ul>
//     *     <li>Base Type</li>
//     *         <p> - if the index is not set, it will be the first argument that matches the BaseType.
//     *     <li>{@link com.github.breadmoirai.bot.framework.command.arg.CommandArgument CommandArgument.class}</li>
//     *         <p> - if the index is not set, it will be the first argument that matches the BaseType.
//     *         <p> - if the argument passed to this parameter is {@code not-null},
//     *     it is guaranteed that <code>{@link com.github.breadmoirai.bot.framework.command.arg.CommandArgument arg}.{@link com.github.breadmoirai.bot.framework.command.arg.CommandArgument#getAsType(Class) getAsType}(baseType).{@link java.util.Optional#isPresent() isPresent()}</code> returns {@code true}.
//     *     <li>List.class</li>
//     * </ul>
//     *
//     * @param paramType
//     *
//     * @return
//     */
//    public CommandParameterBuilder setIntendedType(Class<?> paramType) {
//        this.paramType = paramType;
//        return this;
//    }

    @Override
    public CommandParameterBuilder setFlags(int flags) {
        this.flags = flags;
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
    public <T> CommandParameterBuilder setBaseType(Class<T> type) {
        return setBaseType(type, ArgumentTypes.getParser(type));
    }

    @Override
    public <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        this.type = type;
        this.parser = new ArgumentParser<>(predicate, mapper);
        return this;
    }

    @Override
    public CommandParameterBuilder setOptional(boolean mustBePresent) {
        this.mustBePresent = mustBePresent;
        return this;
    }

    @Override
    public CommandParameterBuilder setOnParamNotFound(BiConsumer<CommandEvent, CommandParameter> onParamNotFound) {
        this.onParamNotFound = onParamNotFound;
        return this;
    }

    @Override
    public CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator) {
        configurator.accept(this);
        return this;
    }


    @Override
    public CommandParameter build() {
        if (parser == null) throw new MissingTypeMapperException(methodName, paramName);
        ArgumentTypeMapper<?> mapper = parser;
        if (paramType == CommandArgument.class) {
            mapper = (arg, flags1) -> parser.test(arg, flags1) ? Optional.of(arg) : Optional.empty();
        }
        final CommandParameterImpl commandParameter = new CommandParameterImpl(type, flags, index, width, mapper, mustBePresent, onParamNotFound);
        if (collectorSupplier != null) {
            @SuppressWarnings("unchecked") final Collector<Object, Object, Object> collector = (Collector<Object, Object, Object>) collectorSupplier.apply(commandParameter.getType());
            return new CommandParameterCollectionImpl(commandParameter, collector);
        }
        return commandParameter;
    }

}
