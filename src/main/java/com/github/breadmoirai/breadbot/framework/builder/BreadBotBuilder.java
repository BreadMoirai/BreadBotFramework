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

package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.BreadBot;
import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandHandleImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertiesManagerImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandResultManagerImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.builder.CommandHandleBuilderFactoryImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.builder.CommandHandleBuilderInternal;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.CommandEventFactory;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventFactoryImpl;
import com.github.breadmoirai.breadbot.framework.internal.BreadBotClientImpl;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CommandParameterTypeManagerImpl;
import com.github.breadmoirai.breadbot.plugins.prefix.PrefixPlugin;
import com.github.breadmoirai.breadbot.plugins.prefix.UnmodifiablePrefixPlugin;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.Checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BreadBotBuilder implements
        CommandPluginBuilder,
        CommandHandleBuilderFactory,
        CommandParameterManagerBuilder,
        CommandResultManagerBuilder,
        CommandPropertiesManager {

//    private static final Logger LOG = LoggerFactory.getLogger(BreadBotClientBuilder.class);

    private final List<CommandPlugin> plugins;
    private final CommandPropertiesManagerImpl commandProperties;
    private final CommandParameterTypeManagerImpl argumentTypes;
    private final CommandHandleBuilderFactoryImpl factory;
    private final List<CommandHandleBuilderInternal> commandBuilders;
    private final List<Command> commands;
    private final CommandResultManagerImpl resultManager;
    private Predicate<Message> preProcessPredicate;
    private CommandEventFactory commandEventFactory;
    private boolean shouldEvaluateCommandOnMessageUpdate = false;

    public BreadBotBuilder() {
        plugins = new ArrayList<>();
        commandProperties = new CommandPropertiesManagerImpl();
        argumentTypes = new CommandParameterTypeManagerImpl();
        factory = new CommandHandleBuilderFactoryImpl(this);
        commandBuilders = new ArrayList<>();
        commands = new ArrayList<>();
        resultManager = new CommandResultManagerImpl();
    }

    @Override
    public BreadBotBuilder addPlugin(Collection<CommandPlugin> plugins) {
        Checks.noneNull(plugins, "plugins");
        for (CommandPlugin module : plugins) {
            module.initialize(this);
        }
        this.plugins.addAll(plugins);
        return this;
    }

    @Override
    public BreadBotBuilder addPlugin(CommandPlugin plugin) {
        Checks.notNull(plugin, "plugin");
        this.plugins.add(plugin);
        plugin.initialize(this);
        return this;
    }

    @Override
    public boolean hasPlugin(Class<? extends CommandPlugin> pluginClass) {
        return pluginClass != null && plugins.stream().map(Object::getClass).anyMatch(pluginClass::isAssignableFrom);
    }

    @Override
    public <T extends CommandPlugin> T getPlugin(Class<T> pluginClass) {
        return pluginClass == null ? null : plugins.stream().filter(module -> pluginClass.isAssignableFrom(module.getClass())).map(pluginClass::cast).findAny().orElse(null);
    }

    public BreadBotBuilder addCommand(Command command) {
        commands.add(command);
        return this;
    }

    @Override
    public CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        Checks.notNull(onCommand, "onCommand");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(onCommand);
        commandBuilders.add(commandHandle);
        return commandHandle;
    }

    @Override
    public BreadBotBuilder addStaticPrefix(String prefix) {
        CommandPluginBuilder.super.addStaticPrefix(prefix);
        return this;
    }

    @Override
    public BreadBotBuilder addOwnerPlugin(long... owners) {
        CommandPluginBuilder.super.addOwnerPlugin(owners);
        return this;
    }

    @Override
    public BreadBotBuilder addAdminPlugin() {
        CommandPluginBuilder.super.addAdminPlugin();
        return this;
    }

    @Override
    public BreadBotBuilder addAdminPlugin(Predicate<Member> isAdmin) {
        CommandPluginBuilder.super.addAdminPlugin(isAdmin);
        return this;
    }

    @Override
    public CommandHandleBuilder createCommand(Class<?> commandClass) {
        Checks.notNull(commandClass, "commandClass");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandClass);
        commandBuilders.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Object commandObject) {
        Checks.notNull(commandObject, "commandObject");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandObject);
        commandBuilders.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Supplier<?> commandSupplier) {
        Checks.notNull(commandSupplier, "commandSupplier");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandSupplier);
        commandBuilders.add(commandHandle);
        return commandHandle;
    }

    @Override
    public List<CommandHandleBuilder> createCommands(String packageName) {
        Checks.notNull(packageName, "packageName");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(packageName);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Class<?> commandClass) {
        Checks.notNull(commandClass, "commandClass");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandClass);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Object commandObject) {
        Checks.notNull(commandObject, "commandObject");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandObject);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Supplier<?> commandSupplier) {
        Checks.notNull(commandSupplier, "commandSupplier");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandSupplier, commandSupplier.get());
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        Checks.noneNull(commandClasses, "commandClasses");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromClasses(commandClasses);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromObjects(Collection<?> commandObjects) {
        Checks.noneNull(commandObjects, "commandObjects");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromObjects(commandObjects);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier) {
        Checks.noneNull(commandSupplier, "commandSuppliers");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromSuppliers(commandSupplier);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public BreadBotBuilder addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(onCommand, configurator);
        return this;
    }

    @Override
    public BreadBotBuilder addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandClass, configurator);
        return this;
    }

    @Override
    public BreadBotBuilder addCommand(Class<?> commandClass) {
        CommandHandleBuilderFactory.super.addCommand(commandClass);
        return this;
    }

    @Override
    public BreadBotBuilder addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandObject, configurator);
        return this;
    }

    @Override
    public BreadBotBuilder addCommand(Object commandObject) {
        CommandHandleBuilderFactory.super.addCommand(commandObject);
        return this;
    }

    @Override
    public BreadBotBuilder addCommand(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandSupplier, configurator);
        return this;
    }

    @Override
    public BreadBotBuilder addCommand(Supplier<?> commandSupplier) {
        CommandHandleBuilderFactory.super.addCommand(commandSupplier);
        return this;
    }

    @Override
    public BreadBotBuilder addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommands(packageName, configurator);
        return this;
    }

    @Override
    public BreadBotBuilder addCommands(String packageName) {
        CommandHandleBuilderFactory.super.addCommands(packageName);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromClasses(Consumer<CommandHandleBuilder> configurator, Class<?>... commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(configurator, commandClasses);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromClasses(Class<?>... commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromObjects(Consumer<CommandHandleBuilder> configurator, Object... commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(configurator, commandObjects);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromObjects(Object... commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromSuppliers(Consumer<CommandHandleBuilder> configurator, Supplier<?>... commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(configurator, commandSuppliers);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromClasses(Collection<Class<?>> commandClasses, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses, configurator);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromClasses(Collection<Class<?>> commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromObjects(Collection<?> commandObjects, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects, configurator);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromObjects(Collection<?> commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers, configurator);
        return this;
    }

    @Override
    public BreadBotBuilder addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers);
        return this;
    }

    @Override
    public BreadBotBuilder clearCommandModifiers(Class<?> propertyType) {
        commandProperties.clearCommandModifiers(propertyType);
        return this;
    }

    @Override
    public BreadBotBuilder clearParameterModifiers(Class<?> parameterType) {
        commandProperties.clearParameterModifiers(parameterType);
        return this;
    }

    @Override
    public <T> BreadBotBuilder bindCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        commandProperties.bindCommandModifier(propertyType, configurator);
        for (CommandHandleBuilder commandBuilder : commandBuilders) {
            applyCommandModifierRecursive(propertyType, configurator, commandBuilder);
        }
        return this;
    }

    private <T> void applyCommandModifierRecursive(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator, CommandHandleBuilder commandBuilder) {
        if (commandBuilder.hasProperty(propertyType)) {
            final T property = commandBuilder.getProperty(propertyType);
            configurator.accept(property, commandBuilder);
        }
        for (CommandHandleBuilder c : commandBuilder.getChildren()) {
            applyCommandModifierRecursive(propertyType, configurator, c);
        }
    }

    @Override
    public <T> BreadBotBuilder bindParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        commandProperties.bindParameterModifier(propertyType, configurator);
        for (CommandHandleBuilder c : commandBuilders) {
            applyParameterModifierRecursive(propertyType, configurator, c);
        }
        return this;
    }

    private <T> void applyParameterModifierRecursive(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator, CommandHandleBuilder commandBuilder) {
        for (CommandParameterBuilder parameterBuilder : commandBuilder.getParameters()) {
            if (parameterBuilder.hasProperty(propertyType)) {
                final T t = parameterBuilder.getProperty(propertyType);
                configurator.accept(t, parameterBuilder);
            }
        }
        for (CommandHandleBuilder commandHandleBuilder : commandBuilder.getChildren()) {
            applyParameterModifierRecursive(propertyType, configurator, commandBuilder);
        }
    }

    @Override
    public void applyModifiers(CommandHandleBuilder builder) {
        commandProperties.applyModifiers(builder);
    }

    @Override
    public <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        return commandProperties.getCommandModifier(propertyType);
    }

    @Override
    public <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        commandProperties.applyCommandModifier(propertyType, builder);
    }

    @Override
    public void applyModifiers(CommandParameterBuilder builder) {
        commandProperties.applyModifiers(builder);
    }

    @Override
    public <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        return commandProperties.getParameterModifier(propertyType);
    }

    @Override
    public <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        commandProperties.applyParameterModifier(propertyType, builder);
    }

    @Override
    public <T> BreadBotBuilder bindPreprocessorFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorFunction> factory) {
        commandProperties.bindPreprocessorFactory(identifier, propertyType, factory);
        return this;
    }

    @Override
    public <T> BreadBotBuilder bindPreprocessorPredicateFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorPredicate> factory) {
        commandProperties.bindPreprocessorPredicateFactory(identifier, propertyType, factory);
        return this;
    }

    @Override
    public BreadBotBuilder bindPreprocessor(String identifier, Class<?> propertyType, CommandPreprocessorFunction function) {
        commandProperties.bindPreprocessor(identifier, propertyType, function);
        return this;
    }

    @Override
    public BreadBotBuilder bindPreprocessorPredicate(String identifier, Class<?> propertyType, CommandPreprocessorPredicate predicate) {
        commandProperties.bindPreprocessorPredicate(identifier, propertyType, predicate);
        return this;
    }

    @Override
    public List<String> getPreprocessorPriorityList() {
        return commandProperties.getPreprocessorPriorityList();
    }

    @Override
    public BreadBotBuilder setPreprocessorPriority(String... identifiers) {
        commandProperties.setPreprocessorPriority(identifiers);
        return this;
    }

    @Override
    public BreadBotBuilder setPreprocessorPriority(List<String> identifierList) {
        commandProperties.setPreprocessorPriority(identifierList);
        return this;
    }

    @Override
    public Comparator<CommandPreprocessor> getPriorityComparator() {
        return commandProperties.getPriorityComparator();
    }

    @Override
    public <T> BreadBotBuilder bindTypeParser(Class<T> type, TypeParser<T> parser) {
        argumentTypes.bindTypeParser(type, parser);
        return this;
    }

    @Override
    public <T> TypeParser<T> getTypeParser(Class<T> type) {
        return argumentTypes.getTypeParser(type);
    }

    @Override
    public BreadBotBuilder clearTypeModifiers(Class<?> parameterType) {
        argumentTypes.clearTypeModifiers(parameterType);
        return this;
    }

    @Override
    public BreadBotBuilder bindTypeModifier(Class<?> parameterType, Consumer<CommandParameterBuilder> modifier) {
        argumentTypes.bindTypeModifier(parameterType, modifier);
        for (CommandHandleBuilderInternal commandBuilder : commandBuilders) {
            applyTypeModifierRecursive(commandBuilder, parameterType, modifier);
        }
        return this;
    }

    private void applyTypeModifierRecursive(CommandHandleBuilder commandBuilder, Class<?> parameterType, Consumer<CommandParameterBuilder> modifier) {
        for (CommandParameterBuilder commandParameterBuilder : commandBuilder.getParameters()) {
            if (commandParameterBuilder.getDeclaringParameter().getType() == parameterType) {
                modifier.accept(commandParameterBuilder);
            }
        }
        for (CommandHandleBuilder subCommand : commandBuilder.getChildren()) {
            applyTypeModifierRecursive(subCommand, parameterType, modifier);
        }
    }

    @Override
    public void applyTypeModifiers(CommandParameterBuilder parameterBuilder) {
        argumentTypes.applyTypeModifiers(parameterBuilder);
    }

    @Override
    public <T> BreadBotBuilder bindResultHandler(Class<T> resultType, CommandResultHandler<T> handler) {
        resultManager.bindResultHandler(resultType, handler);
        return this;
    }

    @Override
    public <T> CommandResultHandler<? super T> getResultHandler(Class<T> resultType) {
        return resultManager.getResultHandler(resultType);
    }

    /**
     * Sets a predicate to be used on each message before processing it. This will override any existing predicates.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     * @return this
     */
    public BreadBotBuilder setPreProcessPredicate(Predicate<Message> predicate) {
        preProcessPredicate = predicate;
        return this;
    }

    /**
     * Appends a predicate to be used on each message before processing it.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     * @return this
     */
    public BreadBotBuilder addPreProcessPredicate(Predicate<Message> predicate) {
        if (preProcessPredicate == null) {
            preProcessPredicate = predicate;
        } else {
            preProcessPredicate = preProcessPredicate.and(predicate);
        }
        return this;
    }

    /**
     * Not much use for this at the moment.
     */
    public BreadBotBuilder setEventFactory(CommandEventFactory commandEventFactory) {
        this.commandEventFactory = commandEventFactory;
        return this;
    }


    /**
     * This will allow messages to be re-evaluated on message edit.
     * This will also evaluate commands that are unpinned.
     * Will ignore messages that are pinned.
     *
     * @param shouldEvaluateCommandOnMessageUpdate By default this is {@code true}.
     * @return this
     */
    public BreadBotBuilder setEvaluateCommandOnMessageUpdate(boolean shouldEvaluateCommandOnMessageUpdate) {
        this.shouldEvaluateCommandOnMessageUpdate = shouldEvaluateCommandOnMessageUpdate;
        return this;
    }

    /**
     * Builds the BreadBotClient with the provided EventManager.
     * If an {@link PrefixPlugin} has not been provided, a {@link UnmodifiablePrefixPlugin new UnmodifiablePrefixPlugin("!")} is provided.
     *
     * @return a new BreadBotClient. This must be added to JDA with {@link net.dv8tion.jda.core.JDABuilder#addEventListener(Object...)}
     */
    public BreadBot build() {
        if (!hasPlugin(PrefixPlugin.class)) plugins.add(new UnmodifiablePrefixPlugin("!"));
        if (commandEventFactory == null)
            commandEventFactory = new CommandEventFactoryImpl(getPlugin(PrefixPlugin.class));
        final List<CommandHandleImpl> build = commandBuilders.stream().map(o -> o.build(null)).collect(Collectors.toList());
        commands.addAll(build);
        return new BreadBotClientImpl(plugins, commands, resultManager, argumentTypes, commandEventFactory, preProcessPredicate, shouldEvaluateCommandOnMessageUpdate);
    }
}