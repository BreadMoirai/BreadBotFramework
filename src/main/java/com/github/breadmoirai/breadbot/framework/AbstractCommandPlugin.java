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
import net.dv8tion.jda.api.entities.Message;

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

    @Override
    public final void initialize(BreadBotBuilder builder) {
        this.builder = builder;
        initialize();
        this.builder = null;
    }

    protected abstract void initialize();

    final protected CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        return builder.createCommand(onCommand);
    }

    final protected void addEmptyCommand(Consumer<CommandHandleBuilder> configurator) {
        builder.addEmptyCommand(configurator);
    }

    final protected CommandHandleBuilder createCommand(Class<?> commandClass) {
        return builder.createCommand(commandClass);
    }

    final protected CommandHandleBuilder createCommand(Object commandObject) {
        return builder.createCommand(commandObject);
    }

    final protected CommandHandleBuilder createCommand(Supplier<?> commandSupplier) {
        return builder.createCommand(commandSupplier);
    }

    final protected List<CommandHandleBuilder> createCommands(String packageName) {
        return builder.createCommands(packageName);
    }

    final protected List<CommandHandleBuilder> createCommands(Class<?> commandClass) {
        return builder.createCommands(commandClass);
    }

    final protected List<CommandHandleBuilder> createCommands(Object commandObject) {
        return builder.createCommands(commandObject);
    }

    final protected List<CommandHandleBuilder> createCommands(Supplier<?> commandSupplier) {
        return builder.createCommands(commandSupplier);
    }

    final protected List<CommandHandleBuilder> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        return builder.createCommandsFromClasses(commandClasses);
    }

    final protected List<CommandHandleBuilder> createCommandsFromObjects(Collection<?> commandObjects) {
        return builder.createCommandsFromObjects(commandObjects);
    }

    final protected List<CommandHandleBuilder> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier) {
        return builder.createCommandsFromSuppliers(commandSupplier);
    }

    final protected void addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        builder.addCommand(onCommand, configurator);
    }

    final protected void addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        builder.addCommand(commandClass, configurator);
    }

    final protected void addCommand(Class<?> commandClass) {
        builder.addCommand(commandClass);
    }

    final protected void addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        builder.addCommand(commandObject, configurator);
    }

    final protected void addCommand(Object commandObject) {
        builder.addCommand(commandObject);
    }

    final protected void addCommand(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        builder.addCommand(commandSupplier, configurator);
    }

    final protected void addCommand(Supplier<?> commandSupplier) {
        builder.addCommand(commandSupplier);
    }

    final protected void addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        builder.addCommands(packageName, configurator);
    }

    final protected void addCommands(String packageName) {
        builder.addCommands(packageName);
    }

    final protected void addCommandsFromClasses(Consumer<CommandHandleBuilder> configurator,
                                                Class<?>... commandClasses) {
        builder.addCommandsFromClasses(configurator, commandClasses);
    }

    final protected void addCommandsFromClasses(Class<?>... commandClasses) {
        builder.addCommandsFromClasses(commandClasses);
    }

    final protected void addCommandsFromObjects(Consumer<CommandHandleBuilder> configurator, Object... commandObjects) {
        builder.addCommandsFromObjects(configurator, commandObjects);
    }

    final protected void addCommandsFromObjects(Object... commandObjects) {
        builder.addCommandsFromObjects(commandObjects);
    }

    final protected void addCommandsFromSuppliers(Consumer<CommandHandleBuilder> configurator,
                                                  Supplier<?>... commandSuppliers) {
        builder.addCommandsFromSuppliers(configurator, commandSuppliers);
    }

    final protected void addCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        builder.addCommandsFromSuppliers(commandSuppliers);
    }

    final protected void addCommandsFromClasses(Collection<Class<?>> commandClasses,
                                                Consumer<CommandHandleBuilder> configurator) {
        builder.addCommandsFromClasses(commandClasses, configurator);
    }

    final protected void addCommandsFromClasses(Collection<Class<?>> commandClasses) {
        builder.addCommandsFromClasses(commandClasses);
    }

    final protected void addCommandsFromObjects(Collection<?> commandObjects,
                                                Consumer<CommandHandleBuilder> configurator) {
        builder.addCommandsFromObjects(commandObjects, configurator);
    }

    final protected void addCommandsFromObjects(Collection<?> commandObjects) {
        builder.addCommandsFromObjects(commandObjects);
    }

    final protected void addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers,
                                                  Consumer<CommandHandleBuilder> configurator) {
        builder.addCommandsFromSuppliers(commandSuppliers, configurator);
    }

    final protected void addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers) {
        builder.addCommandsFromSuppliers(commandSuppliers);
    }

    final protected void clearCommandModifiers(Class<?> propertyType) {
        builder.clearCommandModifiers(propertyType);
    }

    final protected void clearParameterModifiers(Class<?> parameterType) {
        builder.clearParameterModifiers(parameterType);
    }

    final protected <T> void bindCommandModifier(Class<T> propertyType,
                                                 BiConsumer<T, CommandHandleBuilder> configurator) {
        builder.bindCommandModifier(propertyType, configurator);
    }

    final protected <T> void bindParameterModifier(Class<T> propertyType,
                                                   BiConsumer<T, CommandParameterBuilder> configurator) {
        builder.bindParameterModifier(propertyType, configurator);
    }

    final protected void applyModifiers(CommandHandleBuilder builder) {
        this.builder.applyModifiers(builder);
    }

    final protected <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        return builder.getCommandModifier(propertyType);
    }

    final protected <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        this.builder.applyCommandModifier(propertyType, builder);
    }

    final protected void applyModifiers(CommandParameterBuilder builder) {
        this.builder.applyModifiers(builder);
    }

    final protected <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        return builder.getParameterModifier(propertyType);
    }

    final protected <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        this.builder.applyParameterModifier(propertyType, builder);
    }

    final protected <T> void bindPreprocessorFactory(String identifier, Class<T> propertyType,
                                                     Function<T, CommandPreprocessorFunction> factory) {
        builder.bindPreprocessorFactory(identifier, propertyType, factory);
    }

    final protected <T> void bindPreprocessorPredicateFactory(String identifier, Class<T> propertyType,
                                                              Function<T, CommandPreprocessorPredicate> factory) {
        builder.bindPreprocessorPredicateFactory(identifier, propertyType, factory);
    }

    final protected void bindPreprocessor(String identifier, Class<?> propertyType,
                                          CommandPreprocessorFunction function) {
        builder.bindPreprocessor(identifier, propertyType, function);
    }

    final protected void bindPreprocessorPredicate(String identifier, Class<?> propertyType,
                                                   CommandPreprocessorPredicate predicate) {
        builder.bindPreprocessorPredicate(identifier, propertyType, predicate);
    }

    final protected List<String> getPreprocessorPriorityList() {
        return builder.getPreprocessorPriorityList();
    }

    final protected void setPreprocessorPriority(String... identifiers) {
        builder.setPreprocessorPriority(identifiers);
    }

    final protected void setPreprocessorPriority(List<String> identifierList) {
        builder.setPreprocessorPriority(identifierList);
    }

    final protected Comparator<CommandPreprocessor> getPriorityComparator() {
        return builder.getPriorityComparator();
    }

    final protected <T> void bindTypeParser(Class<T> type, TypeParser<T> parser) {
        builder.bindTypeParser(type, parser);
    }

    final protected <T> TypeParser<T> getTypeParser(Class<T> type) {
        return builder.getTypeParser(type);
    }

    final protected void clearTypeModifiers(Class<?> parameterType) {
        builder.clearTypeModifiers(parameterType);
    }

    final protected void bindTypeModifier(Class<?> parameterType, Consumer<CommandParameterBuilder> modifier) {
        builder.bindTypeModifier(parameterType, modifier);
    }

    final protected void applyTypeModifiers(CommandParameterBuilder parameterBuilder) {
        builder.applyTypeModifiers(parameterBuilder);
    }

    final protected <T> void bindResultHandler(Class<T> resultType, CommandResultHandler<T> handler) {
        builder.bindResultHandler(resultType, handler);
    }

    final protected <T> CommandResultHandler<? super T> getResultHandler(Class<T> resultType) {
        return builder.getResultHandler(resultType);
    }

    /**
     * Sets a predicate to be used on each message before processing it. This will override any existing predicates.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     */
    final protected void setPreProcessPredicate(Predicate<Message> predicate) {
        builder.setPreProcessPredicate(predicate);
    }

    /**
     * Appends a predicate to be used on each message before processing it.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     */
    final protected void addPreProcessPredicate(Predicate<Message> predicate) {
        builder.addPreProcessPredicate(predicate);
    }

    /**
     * This will allow messages to be re-evaluated on message edit.
     * This will also evaluate commands that are unpinned.
     * Will ignore messages that are pinned.
     *
     * @param shouldEvaluateCommandOnMessageUpdate By default this is {@code false}.
     */
    final protected void setEvaluateCommandOnMessageUpdate(boolean shouldEvaluateCommandOnMessageUpdate) {
        builder.setEvaluateCommandOnMessageUpdate(shouldEvaluateCommandOnMessageUpdate);
    }

    final protected List<CommandHandleBuilder> createCommandsFromClasses(Class<?>... commandClasses) {
        return builder.createCommandsFromClasses(commandClasses);
    }

    final protected List<CommandHandleBuilder> createCommandsFromObjects(Object... commandObjects) {
        return builder.createCommandsFromObjects(commandObjects);
    }

    final protected List<CommandHandleBuilder> createCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        return builder.createCommandsFromSuppliers(commandSuppliers);
    }
}
