package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class CommandHandleBuilderCollection implements CommandHandleBuilder {

    private final List<CommandHandleBuilder> handleBuilders;

    public CommandHandleBuilderCollection(List<CommandHandleBuilder> handleBuilders) {
        this.handleBuilders = handleBuilders;
    }

    @Override
    public CommandHandleBuilder addSubCommand(@Nullable Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        handleBuilders.forEach(o -> o.addSubCommand(onCommand, configurator));
		return this;
    }

    @Override
    public CommandHandleBuilder createSubCommand(Consumer<CommandEvent> onCommand) {
        handleBuilders.forEach(o -> o.createSubCommand(onCommand));
		return this;
    }

    @Override
    public CommandHandleBuilder setKeys(String... key) {
        handleBuilders.forEach(o -> o.setKeys(key));
		return this;
    }

    @Override
    public CommandHandleBuilder setName(String name) {
        handleBuilders.forEach(o -> o.setName(name));
		return this;
    }

    @Override
    public CommandHandleBuilder setGroup(String group) {
        handleBuilders.forEach(o -> o.setGroup(group));
		return this;
    }

    @Override
    public CommandHandleBuilder setDescription(String description) {
        handleBuilders.forEach(o -> o.setDescription(description));
		return this;
    }

    @Override
    public boolean containsProperty(Class<?> propertyType) {
        throw new UnsupportedOperationException("This operation is unsupported for a collection of builders.");
    }

    @Override
    public <T> T getProperty(Class<T> propertyType) {
        throw new UnsupportedOperationException("This operation is unsupported for a collection of builders.");
    }

    @Override
    public <T> CommandHandleBuilder putProperty(Class<? super T> type, T property) {
        handleBuilders.forEach(o -> o.putProperty(type, property));
		return this;
    }

    @Override
    public CommandHandleBuilder putProperty(Object property) {
        handleBuilders.forEach(o -> o.putProperty(property));
		return this;
    }

    @Override
    public <T> CommandHandleBuilder applyProperty(Class<? super T> type, T property) {
        handleBuilders.forEach(o -> o.applyProperty(type, property));
		return this;
    }

    @Override
    public <T> CommandHandleBuilder applyProperty(T property) {
        handleBuilders.forEach(o -> o.applyProperty(property));
		return this;
    }

    @Override
    public CommandHandleBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        handleBuilders.forEach(o -> o.addPreprocessorFunction(identifier, function));
		return this;
    }

    @Override
    public CommandHandleBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        handleBuilders.forEach(o -> o.addPreprocessorPredicate(identifier, predicate));
		return this;
    }

    @Override
    public CommandHandleBuilder addPreprocessor(CommandPreprocessor preprocessor) {
        handleBuilders.forEach(o -> o.addPreprocessor(preprocessor));
		return this;
    }

    @Override
    public List<CommandPreprocessor> getPreprocessors() {
        throw new UnsupportedOperationException("This operation is unsupported for a collection of builders.");
    }

    @Override
    public CommandHandleBuilder sortPreprocessors(Comparator<CommandPreprocessor> comparator) {
        handleBuilders.forEach(o -> o.sortPreprocessors(comparator));
		return this;
    }
    
    @Override
    public Class<?> getDeclaringClass() {
        throw new UnsupportedOperationException("This operation is unsupported for a collection of builders.");
    }

    @Override
    public Method getDeclaringMethod() {
        throw new UnsupportedOperationException("This operation is unsupported for a collection of builders.");
    }

    @Override
    public BreadBotClientBuilder getClientBuilder() {
        throw new UnsupportedOperationException("This operation is unsupported for a collection of builders.");
    }

    @Override
    public CommandHandle build(BreadBotClient client) {
        throw new UnsupportedOperationException("This operation is unsupported for a collection of builders.");
    }
}
