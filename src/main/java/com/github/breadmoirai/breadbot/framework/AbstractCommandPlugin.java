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
package com.github.breadmoirai.breadbot.framework;

import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import net.dv8tion.jda.core.entities.Message;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public abstract class AbstractCommandPlugin implements CommandPlugin {
    private BreadBotBuilder builder;
    private Consumer<BreadBot> onReady;

    @Override
    public final void initialize(BreadBotBuilder builder) {
        this.builder = builder;
        initialize();
    }

    abstract void initialize();

    @Override
    public final void onBreadReady(BreadBot client) {
        builder = null;
        if (onReady != null) {
            onReady.accept(client);
            onReady = null;
        }
    }

    final public void onBreadReady(Consumer<BreadBot> onReady) {
        this.onReady = onReady;
    }

    final public CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        return builder.createCommand(onCommand);
    }

    final public BreadBotBuilder addEmptyCommand(Consumer<CommandHandleBuilder> configurator) {
        return builder.addEmptyCommand(configurator);
    }

    final public CommandHandleBuilder createCommand(Class<?> commandClass) {
        return builder.createCommand(commandClass);
    }

    final public CommandHandleBuilder createCommand(Object commandObject) {
        return builder.createCommand(commandObject);
    }

    final public CommandHandleBuilder createCommand(Supplier<?> commandSupplier) {
        return builder.createCommand(commandSupplier);
    }

    final public List<CommandHandleBuilder> createCommands(String packageName) {
        return builder.createCommands(packageName);
    }

    final public List<CommandHandleBuilder> createCommands(Class<?> commandClass) {
        return builder.createCommands(commandClass);
    }

    final public List<CommandHandleBuilder> createCommands(Object commandObject) {
        return builder.createCommands(commandObject);
    }

    final public List<CommandHandleBuilder> createCommands(Supplier<?> commandSupplier) {
        return builder.createCommands(commandSupplier);
    }

    final public List<CommandHandleBuilder> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        return builder.createCommandsFromClasses(commandClasses);
    }

    final public List<CommandHandleBuilder> createCommandsFromObjects(Collection<?> commandObjects) {
        return builder.createCommandsFromObjects(commandObjects);
    }

    final public List<CommandHandleBuilder> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier) {
        return builder.createCommandsFromSuppliers(commandSupplier);
    }

    final public BreadBotBuilder addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        return builder.addCommand(onCommand, configurator);
    }

    final public BreadBotBuilder addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        return builder.addCommand(commandClass, configurator);
    }

    final public BreadBotBuilder addCommand(Class<?> commandClass) {
        return builder.addCommand(commandClass);
    }

    final public BreadBotBuilder addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        return builder.addCommand(commandObject, configurator);
    }

    final public BreadBotBuilder addCommand(Object commandObject) {
        return builder.addCommand(commandObject);
    }

    final public BreadBotBuilder addCommand(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        return builder.addCommand(commandSupplier, configurator);
    }

    final public BreadBotBuilder addCommand(Supplier<?> commandSupplier) {
        return builder.addCommand(commandSupplier);
    }

    final public BreadBotBuilder addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        return builder.addCommands(packageName, configurator);
    }

    final public BreadBotBuilder addCommands(String packageName) {
        return builder.addCommands(packageName);
    }

    final public BreadBotBuilder addCommandsFromClasses(Consumer<CommandHandleBuilder> configurator, Class<?>... commandClasses) {
        return builder.addCommandsFromClasses(configurator, commandClasses);
    }

    final public BreadBotBuilder addCommandsFromClasses(Class<?>... commandClasses) {
        return builder.addCommandsFromClasses(commandClasses);
    }

    final public BreadBotBuilder addCommandsFromObjects(Consumer<CommandHandleBuilder> configurator, Object... commandObjects) {
        return builder.addCommandsFromObjects(configurator, commandObjects);
    }

    final public BreadBotBuilder addCommandsFromObjects(Object... commandObjects) {
        return builder.addCommandsFromObjects(commandObjects);
    }

    final public BreadBotBuilder addCommandsFromSuppliers(Consumer<CommandHandleBuilder> configurator, Supplier<?>... commandSuppliers) {
        return builder.addCommandsFromSuppliers(configurator, commandSuppliers);
    }

    final public BreadBotBuilder addCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        return builder.addCommandsFromSuppliers(commandSuppliers);
    }

    final public BreadBotBuilder addCommandsFromClasses(Collection<Class<?>> commandClasses, Consumer<CommandHandleBuilder> configurator) {
        return builder.addCommandsFromClasses(commandClasses, configurator);
    }

    final public BreadBotBuilder addCommandsFromClasses(Collection<Class<?>> commandClasses) {
        return builder.addCommandsFromClasses(commandClasses);
    }

    final public BreadBotBuilder addCommandsFromObjects(Collection<?> commandObjects, Consumer<CommandHandleBuilder> configurator) {
        return builder.addCommandsFromObjects(commandObjects, configurator);
    }

    final public BreadBotBuilder addCommandsFromObjects(Collection<?> commandObjects) {
        return builder.addCommandsFromObjects(commandObjects);
    }

    final public BreadBotBuilder addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers, Consumer<CommandHandleBuilder> configurator) {
        return builder.addCommandsFromSuppliers(commandSuppliers, configurator);
    }

    final public BreadBotBuilder addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers) {
        return builder.addCommandsFromSuppliers(commandSuppliers);
    }

    final public BreadBotBuilder clearCommandModifiers(Class<?> propertyType) {
        return builder.clearCommandModifiers(propertyType);
    }

    final public BreadBotBuilder clearParameterModifiers(Class<?> parameterType) {
        return builder.clearParameterModifiers(parameterType);
    }

    final public <T> BreadBotBuilder bindCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        return builder.bindCommandModifier(propertyType, configurator);
    }

    final public <T> BreadBotBuilder bindParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        return builder.bindParameterModifier(propertyType, configurator);
    }

    final public void applyModifiers(CommandHandleBuilder builder) {
        this.builder.applyModifiers(builder);
    }

    final public <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        return builder.getCommandModifier(propertyType);
    }

    final public <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        this.builder.applyCommandModifier(propertyType, builder);
    }

    final public void applyModifiers(CommandParameterBuilder builder) {
        this.builder.applyModifiers(builder);
    }

    final public <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        return builder.getParameterModifier(propertyType);
    }

    final public <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        this.builder.applyParameterModifier(propertyType, builder);
    }

    final public <T> BreadBotBuilder bindPreprocessorFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorFunction> factory) {
        return builder.bindPreprocessorFactory(identifier, propertyType, factory);
    }

    final public <T> BreadBotBuilder bindPreprocessorPredicateFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorPredicate> factory) {
        return builder.bindPreprocessorPredicateFactory(identifier, propertyType, factory);
    }

    final public BreadBotBuilder bindPreprocessor(String identifier, Class<?> propertyType, CommandPreprocessorFunction function) {
        return builder.bindPreprocessor(identifier, propertyType, function);
    }

    final public BreadBotBuilder bindPreprocessorPredicate(String identifier, Class<?> propertyType, CommandPreprocessorPredicate predicate) {
        return builder.bindPreprocessorPredicate(identifier, propertyType, predicate);
    }

    final public List<String> getPreprocessorPriorityList() {
        return builder.getPreprocessorPriorityList();
    }

    final public BreadBotBuilder setPreprocessorPriority(String... identifiers) {
        return builder.setPreprocessorPriority(identifiers);
    }

    final public BreadBotBuilder setPreprocessorPriority(List<String> identifierList) {
        return builder.setPreprocessorPriority(identifierList);
    }

    final public Comparator<CommandPreprocessor> getPriorityComparator() {
        return builder.getPriorityComparator();
    }

    final public <T> BreadBotBuilder bindTypeParser(Class<T> type, TypeParser<T> parser) {
        return builder.bindTypeParser(type, parser);
    }

    final public <T> TypeParser<T> getTypeParser(Class<T> type) {
        return builder.getTypeParser(type);
    }

    final public BreadBotBuilder clearTypeModifiers(Class<?> parameterType) {
        return builder.clearTypeModifiers(parameterType);
    }

    final public BreadBotBuilder bindTypeModifier(Class<?> parameterType, Consumer<CommandParameterBuilder> modifier) {
        return builder.bindTypeModifier(parameterType, modifier);
    }

    final public void applyTypeModifiers(CommandParameterBuilder parameterBuilder) {
        builder.applyTypeModifiers(parameterBuilder);
    }

    final public <T> BreadBotBuilder bindResultHandler(Class<T> resultType, CommandResultHandler<T> handler) {
        return builder.bindResultHandler(resultType, handler);
    }

    final public <T> CommandResultHandler<? super T> getResultHandler(Class<T> resultType) {
        return builder.getResultHandler(resultType);
    }

    /**
     * Sets a predicate to be used on each message before processing it. This will override any existing predicates.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     * @return this
     */
    final public BreadBotBuilder setPreProcessPredicate(Predicate<Message> predicate) {
        return builder.setPreProcessPredicate(predicate);
    }

    /**
     * Appends a predicate to be used on each message before processing it.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     * @return this
     */
    final public BreadBotBuilder addPreProcessPredicate(Predicate<Message> predicate) {
        return builder.addPreProcessPredicate(predicate);
    }

    /**
     * This will allow messages to be re-evaluated on message edit.
     * This will also evaluate commands that are unpinned.
     * Will ignore messages that are pinned.
     *
     * @param shouldEvaluateCommandOnMessageUpdate By default this is {@code false}.
     * @return this
     */
    final public BreadBotBuilder setEvaluateCommandOnMessageUpdate(boolean shouldEvaluateCommandOnMessageUpdate) {
        return builder.setEvaluateCommandOnMessageUpdate(shouldEvaluateCommandOnMessageUpdate);
    }

    final public List<CommandHandleBuilder> createCommandsFromClasses(Class<?>... commandClasses) {
        return builder.createCommandsFromClasses(commandClasses);
    }

    final public List<CommandHandleBuilder> createCommandsFromObjects(Object... commandObjects) {
        return builder.createCommandsFromObjects(commandObjects);
    }

    final public List<CommandHandleBuilder> createCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        return builder.createCommandsFromSuppliers(commandSuppliers);
    }
}
