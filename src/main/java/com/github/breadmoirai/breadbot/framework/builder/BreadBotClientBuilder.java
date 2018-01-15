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

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
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
import com.github.breadmoirai.breadbot.plugins.prefix.StaticPrefixModule;
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

public class BreadBotClientBuilder implements
        BreadBotPluginBuilder,
        CommandHandleBuilderFactory,
        CommandParameterManagerBuilder,
        CommandResultManagerBuilder,
        CommandPropertiesBuilder {

//    private static final Logger LOG = LoggerFactory.getLogger(BreadBotClientBuilder.class);

    private final List<CommandPlugin> modules;
    private final CommandPropertiesManagerImpl commandProperties;
    private final CommandParameterTypeManagerImpl argumentTypes;
    private final CommandHandleBuilderFactoryImpl factory;
    private final List<CommandHandleBuilderInternal> commands;
    private final CommandResultManagerImpl resultManager;
    private Predicate<Message> preProcessPredicate;
    private CommandEventFactory commandEventFactory;
    private boolean shouldEvaluateCommandOnMessageUpdate = false;

    public BreadBotClientBuilder() {
        modules = new ArrayList<>();
        commandProperties = new CommandPropertiesManagerImpl();
        argumentTypes = new CommandParameterTypeManagerImpl();
        factory = new CommandHandleBuilderFactoryImpl(this);
        commands = new ArrayList<>();
        resultManager = new CommandResultManagerImpl();
    }

    @Override
    public BreadBotClientBuilder addPlugin(Collection<CommandPlugin> modules) {
        Checks.noneNull(modules, "modules");
        for (CommandPlugin module : modules) {
            module.initialize(this);
        }
        this.modules.addAll(modules);
        return this;
    }

    @Override
    public BreadBotClientBuilder addPlugin(CommandPlugin module) {
        Checks.notNull(module, "module");
        this.modules.add(module);
        module.initialize(this);
        return this;
    }

    @Override
    public boolean hasPlugin(Class<? extends CommandPlugin> moduleClass) {
        return moduleClass != null && modules.stream().map(Object::getClass).anyMatch(moduleClass::isAssignableFrom);
    }

    @Override
    public <T extends CommandPlugin> T getPlugin(Class<T> moduleClass) {
        return moduleClass == null ? null : modules.stream().filter(module -> moduleClass.isAssignableFrom(module.getClass())).map(moduleClass::cast).findAny().orElse(null);
    }

    @Override
    public CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        Checks.notNull(onCommand, "onCommand");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(onCommand);
        commands.add(commandHandle);
        return commandHandle;
    }

    @Override
    public BreadBotClientBuilder addStaticPrefix(String prefix) {
        BreadBotPluginBuilder.super.addStaticPrefix(prefix);
        return this;
    }

    @Override
    public BreadBotClientBuilder addOwnerPlugin(long... owners) {
        BreadBotPluginBuilder.super.addOwnerPlugin(owners);
        return this;
    }

    @Override
    public BreadBotClientBuilder addAdminPlugin() {
        BreadBotPluginBuilder.super.addAdminPlugin();
        return this;
    }

    @Override
    public BreadBotClientBuilder addAdminPlugin(Predicate<Member> isAdmin) {
        BreadBotPluginBuilder.super.addAdminPlugin(isAdmin);
        return this;
    }

    @Override
    public CommandHandleBuilder createCommand(Class<?> commandClass) {
        Checks.notNull(commandClass, "commandClass");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandClass);
        commands.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Object commandObject) {
        Checks.notNull(commandObject, "commandObject");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandObject);
        commands.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Supplier<?> commandSupplier) {
        Checks.notNull(commandSupplier, "commandSupplier");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandSupplier);
        commands.add(commandHandle);
        return commandHandle;
    }

    @Override
    public List<CommandHandleBuilder> createCommands(String packageName) {
        Checks.notNull(packageName, "packageName");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(packageName);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Class<?> commandClass) {
        Checks.notNull(commandClass, "commandClass");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandClass);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Object commandObject) {
        Checks.notNull(commandObject, "commandObject");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandObject);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Supplier<?> commandSupplier) {
        Checks.notNull(commandSupplier, "commandSupplier");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandSupplier, commandSupplier.get());
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        Checks.noneNull(commandClasses, "commandClasses");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromClasses(commandClasses);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromObjects(Collection<?> commandObjects) {
        Checks.noneNull(commandObjects, "commandObjects");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromObjects(commandObjects);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier) {
        Checks.noneNull(commandSupplier, "commandSuppliers");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromSuppliers(commandSupplier);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public BreadBotClientBuilder addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(onCommand, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandClass, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommand(Class<?> commandClass) {
        CommandHandleBuilderFactory.super.addCommand(commandClass);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandObject, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommand(Object commandObject) {
        CommandHandleBuilderFactory.super.addCommand(commandObject);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommand(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandSupplier, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommand(Supplier<?> commandSupplier) {
        CommandHandleBuilderFactory.super.addCommand(commandSupplier);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommands(packageName, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommands(String packageName) {
        CommandHandleBuilderFactory.super.addCommands(packageName);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromClasses(Consumer<CommandHandleBuilder> configurator, Class<?>... commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(configurator, commandClasses);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromClasses(Class<?>... commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromObjects(Consumer<CommandHandleBuilder> configurator, Object... commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(configurator, commandObjects);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromObjects(Object... commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromSuppliers(Consumer<CommandHandleBuilder> configurator, Supplier<?>... commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(configurator, commandSuppliers);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromClasses(Collection<Class<?>> commandClasses, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromClasses(Collection<Class<?>> commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromObjects(Collection<?> commandObjects, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromObjects(Collection<?> commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder putCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        commandProperties.putCommandModifier(propertyType, configurator);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder appendCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        commandProperties.appendCommandModifier(propertyType, configurator);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder putParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        commandProperties.putParameterModifier(propertyType, configurator);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder appendParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        commandProperties.appendParameterModifier(propertyType, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder applyPropertyModifiers(CommandHandleBuilder builder) {
        commandProperties.applyModifiers(builder);
        return this;
    }

    @Override
    public <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        return commandProperties.getCommandModifier(propertyType);
    }

    @Override
    public <T> BreadBotClientBuilder applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        commandProperties.applyCommandModifier(propertyType, builder);
        return this;
    }

    @Override
    public BreadBotClientBuilder applyPropertyModifiers(CommandParameterBuilder builder) {
        commandProperties.applyModifiers(builder);
        return this;
    }

    @Override
    public <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        return commandProperties.getParameterModifier(propertyType);
    }

    @Override
    public <T> BreadBotClientBuilder applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        commandProperties.applyParameterModifier(propertyType, builder);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder associatePreprocessorFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorFunction> factory) {
        commandProperties.associatePreprocessorFactory(identifier, propertyType, factory);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder associatePreprocessorPredicateFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorPredicate> factory) {
        commandProperties.associatePreprocessorPredicateFactory(identifier, propertyType, factory);
        return this;
    }

    @Override
    public BreadBotClientBuilder associatePreprocessor(String identifier, Class<?> propertyType, CommandPreprocessorFunction function) {
        commandProperties.associatePreprocessor(identifier, propertyType, function);
        return this;
    }

    @Override
    public BreadBotClientBuilder associatePreprocessorPredicate(String identifier, Class<?> propertyType, CommandPreprocessorPredicate predicate) {
        commandProperties.associatePreprocessorPredicate(identifier, propertyType, predicate);
        return this;
    }

    @Override
    public List<String> getPreprocessorPriorityList() {
        return commandProperties.getPreprocessorPriorityList();
    }

    @Override
    public BreadBotClientBuilder setPreprocessorPriority(String... identifiers) {
        commandProperties.setPreprocessorPriority(identifiers);
        return this;
    }

    @Override
    public BreadBotClientBuilder setPreprocessorPriority(List<String> identifierList) {
        commandProperties.setPreprocessorPriority(identifierList);
        return this;
    }

    @Override
    public Comparator<CommandPreprocessor> getPriorityComparator() {
        return commandProperties.getPriorityComparator();
    }

    @Override
    public <T> BreadBotClientBuilder putTypeParser(Class<T> type, TypeParser<T> parser) {
        argumentTypes.putTypeParser(type, parser);
        return this;
    }

    @Override
    public <T> TypeParser<T> getTypeParser(Class<T> type) {
        return argumentTypes.getTypeParser(type);
    }

    @Override
    public BreadBotClientBuilder putTypeModifier(Class<?> parameterType, Consumer<CommandParameterBuilder> modifier) {
        argumentTypes.putTypeModifier(parameterType, modifier);
        return this;
    }

    @Override
    public BreadBotClientBuilder appendTypeModifer(Class<?> parameterType, Consumer<CommandParameterBuilder> modifier) {
        argumentTypes.appendTypeModifer(parameterType, modifier);
        return this;
    }

    @Override
    public void applyTypeModifiers(CommandParameterBuilder parameterBuilder) {
        argumentTypes.applyTypeModifiers(parameterBuilder);
    }

    @Override
    public <T> BreadBotClientBuilder registerResultHandler(Class<T> resultType, CommandResultHandler<T> handler) {
        resultManager.registerResultHandler(resultType, handler);
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
    public BreadBotClientBuilder setPreProcessPredicate(Predicate<Message> predicate) {
        preProcessPredicate = predicate;
        return this;
    }

    /**
     * Appends a predicate to be used on each message before processing it.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     * @return this
     */
    public BreadBotClientBuilder addPreProcessPredicate(Predicate<Message> predicate) {
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
    public BreadBotClientBuilder setEventFactory(CommandEventFactory commandEventFactory) {
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
    public BreadBotClientBuilder setEvaluateCommandOnMessageUpdate(boolean shouldEvaluateCommandOnMessageUpdate) {
        this.shouldEvaluateCommandOnMessageUpdate = shouldEvaluateCommandOnMessageUpdate;
        return this;
    }

    /**
     * Builds the BreadBotClient with the provided EventManager.
     * If an {@link PrefixPlugin} has not been provided, a {@link StaticPrefixModule new DefaultPrefixModule("!")} is provided.
     *
     * @return a new BreadBotClient. This must be added to JDA with {@link net.dv8tion.jda.core.JDABuilder#addEventListener(Object...)}
     */
    public BreadBotClient build() {
        if (!hasPlugin(PrefixPlugin.class)) modules.add(new StaticPrefixModule("!"));
        if (commandEventFactory == null)
            commandEventFactory = new CommandEventFactoryImpl(getPlugin(PrefixPlugin.class));
        return new BreadBotClientImpl(modules, commands, commandProperties, resultManager, argumentTypes, commandEventFactory, preProcessPredicate, shouldEvaluateCommandOnMessageUpdate);
    }
}