/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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

import com.github.breadmoirai.breadbot.framework.internal.BreadBotClientImpl;
import com.github.breadmoirai.breadbot.framework.internal.argument.CommandParameterTypeManagerImpl;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandPropertiesManagerImpl;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandResultManagerImpl;
import com.github.breadmoirai.breadbot.framework.internal.command.builder.CommandHandleBuilderFactoryImpl;
import com.github.breadmoirai.breadbot.framework.internal.command.builder.CommandHandleBuilderInternal;
import com.github.breadmoirai.breadbot.framework.internal.event.CommandEventFactoryImpl;
import com.github.breadmoirai.breadbot.framework.internal.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.internal.parameter.ArgumentTypeMapper;
import com.github.breadmoirai.breadbot.framework.internal.parameter.ArgumentTypePredicate;
import com.github.breadmoirai.breadbot.framework.internal.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.modules.prefix.DefaultPrefixModule;
import com.github.breadmoirai.breadbot.modules.prefix.PrefixModule;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.Checks;

import java.util.*;
import java.util.function.*;

public class BreadBotClientBuilder implements
        CommandHandleBuilderFactory<BreadBotClientBuilder>,
        CommandModuleBuilder<BreadBotClientBuilder>,
        CommandPropertiesBuilder<BreadBotClientBuilder>,
        CommandParameterManagerBuilder<BreadBotClientBuilder>,
        CommandResultManagerBuilder<BreadBotClientBuilder> {

//    private static final Logger LOG = LoggerFactory.getLogger(BreadBotClientBuilder.class);

    private final List<CommandModule> modules;
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
    public BreadBotClientBuilder self() {
        return this;
    }

    @Override
    public BreadBotClientBuilder addModule(Collection<CommandModule> modules) {
        Checks.noneNull(modules, "modules");
        for (CommandModule module : modules) {
            module.initialize(this);
        }
        this.modules.addAll(modules);
        return this;
    }

    @Override
    public BreadBotClientBuilder addModule(CommandModule module) {
        Checks.notNull(module, "module");
        this.modules.add(module);
        module.initialize(this);
        return this;
    }

    @Override
    public boolean hasModule(Class<? extends CommandModule> moduleClass) {
        return moduleClass != null && modules.stream().map(Object::getClass).anyMatch(moduleClass::isAssignableFrom);
    }

    @Override
    public <T extends CommandModule> T getModule(Class<T> moduleClass) {
        return moduleClass == null ? null : modules.stream().filter(module -> moduleClass.isAssignableFrom(module.getClass())).map(moduleClass::cast).findAny().orElse(null);
    }

    @Override
    public BreadBotClientBuilder addCommand(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        List<CommandHandleBuilderInternal> commandsFromSuppliers = factory.createCommandsFromSuppliers(commandSupplier);
        commandsFromSuppliers.forEach(configurator);
        commands.addAll(commandsFromSuppliers);
        return this;
    }

    @Override
    public BreadBotClientBuilder addCommand(Supplier<?> commandSupplier) {
        List<CommandHandleBuilderInternal> commandsFromSuppliers = factory.createCommandsFromSuppliers(commandSupplier);
        commands.addAll(commandsFromSuppliers);
        return this;
    }

    @Override
    public CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        CommandHandleBuilderInternal commandHandle = factory.createCommand(onCommand);
        commands.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Class<?> commandClass) {
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandClass);
        commands.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Object commandObject) {
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandObject);
        commands.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Supplier<?> commandSupplier) {
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandSupplier);
        commands.add(commandHandle);
        return commandHandle;
    }

    @Override
    public List<CommandHandleBuilder> createCommands(String packageName) {
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(packageName);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Class<?> commandClass) {
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandClass);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Object commandObject) {
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandObject);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Supplier<?> commandSupplier) {
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandSupplier, commandSupplier.get());
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromClasses(commandClasses);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromObjects(Collection<?> commandObjects) {
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromObjects(commandObjects);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier) {
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromSuppliers(commandSupplier);
        commands.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public <T> BreadBotClientBuilder putCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        commandProperties.putCommandModifier(propertyType, configurator);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder appendCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        commandProperties.appendCommandModifier(propertyType, configurator);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder putParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        commandProperties.putParameterModifier(propertyType, configurator);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder appendParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        commandProperties.appendParameterModifier(propertyType, configurator);
        return this;
    }

    @Override
    public BreadBotClientBuilder applyModifiers(CommandHandleBuilder builder) {
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
    public BreadBotClientBuilder applyModifiers(CommandParameterBuilder builder) {
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
    public <T> BreadBotClientBuilder registerArgumentMapper(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        argumentTypes.registerArgumentMapper(type, predicate, mapper);
        return this;
    }

    @Override
    public <T> BreadBotClientBuilder registerArgumentMapperSimple(Class<T> type, Predicate<CommandArgument> isType, Function<CommandArgument, T> getAsType) {
        argumentTypes.registerArgumentMapperSimple(type, isType, getAsType);
        return this;
    }

    @Override
    public <T> ArgumentParser<T> getParser(Class<T> type) {
        return argumentTypes.getParser(type);
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

//    /**
//     * Builds a BreadBotClient with an {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager}
//     * <p>
//     * This implementation is as follows:
//     * <pre><code>
//     *     return {@link BreadBotClientBuilder breadBotBuilder}.{@link BreadBotClientBuilder#build build}(new {@link net.dv8tion.jda.core.hooks.AnnotatedEventManager AnnotatedEventManager()});
//     * </code></pre>
//     *
//     * @return The {@link com.github.breadmoirai.breadbot.framework.BreadBotClient} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager}({@link com.github.breadmoirai.breadbot.framework.BreadBotClient#getEventManager client.getEventManager()})
//     */
//    public BreadBotClient buildAnnotated() {
//        return build(new AnnotatedEventManager());
//    }
//
//    /**
//     * Builds a BreadBotClient with an {@link net.dv8tion.jda.core.hooks.InterfacedEventManager}
//     * <p>
//     * This implementation is as follows:
//     * <pre><code>
//     *     return {@link BreadBotClientBuilder breadBotBuilder}.{@link BreadBotClientBuilder#build build}(new {@link net.dv8tion.jda.core.hooks.InterfacedEventManager InterfacedEventManager()});
//     * </code></pre>
//     *
//     * @return The {@link com.github.breadmoirai.breadbot.framework.BreadBotClient} for use with {@link net.dv8tion.jda.core.JDABuilder#setEventManager(IEventManager) JDABuilder#setEventManager}({@link com.github.breadmoirai.breadbot.framework.BreadBotClient#getEventManager client.getEventManager()})
//     */
//    public BreadBotClient buildInterfaced() {
//        return build(new InterfacedEventManager());
//    }

    /**
     * Builds the BreadBotClient with the provided EventManager.
     * If an {@link PrefixModule} has not been provided, a {@link com.github.breadmoirai.breadbot.modules.prefix.DefaultPrefixModule new DefaultPrefixModule("!")} is provided.
     *
     * @return a new BreadBotClient. This must be added to JDA with {@link net.dv8tion.jda.core.JDABuilder#addEventListener(Object...)}
     */
    public BreadBotClient build() {
        if (!hasModule(PrefixModule.class)) modules.add(new DefaultPrefixModule("!"));
        if (commandEventFactory == null)
            commandEventFactory = new CommandEventFactoryImpl(getModule(PrefixModule.class));
        return new BreadBotClientImpl(modules, commands, commandProperties, resultManager, argumentTypes, commandEventFactory, preProcessPredicate, shouldEvaluateCommandOnMessageUpdate);
    }
}