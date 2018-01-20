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

package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.error.NoSuchCommandException;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public interface CommandHandleBuilder extends CommandHandleBuilderFactory {

    CommandHandleBuilder setKeys(String... key);

    CommandHandleBuilder setName(String name);

    CommandHandleBuilder setGroup(String group);

    CommandHandleBuilder setDescription(String description);

    /**
     * Attempts to find a subcommand by the specified name.
     *
     * @param commandName the name of the command. By default this is the name of the method or the name of the inner class. If the subcommand is an inner class and its name ends in {@code "command"}, the name only includes the part preceding {@code "command"}.
     * @return The CommandHandleBuilder of the sub command.
     * @throws NoSuchCommandException when the subcommand is not found
     */
    default CommandHandleBuilder getChild(String commandName) {
        return getChildren().stream().filter(commandHandleBuilder -> commandHandleBuilder.getName().equals(commandName)).findAny().orElseThrow(() -> new NoSuchCommandException("A subcommand by the name of " + commandName + " was not found"));
    }

    /**
     * Attempts to find a subcommand by the specified name and configure it.
     *
     * @param commandName  the name of the command. By default this is the name of the method or the name of the inner class. If the subcommand is an inner class and its name ends in {@code "command"}, the name only includes the part preceding {@code "command"}.
     * @param configurator a consumer that is used to modify the subcommand specified
     * @return this
     * @throws NoSuchCommandException when the subcommand is not found
     */
    default CommandHandleBuilder configureChild(String commandName, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilder builder = getChildren().stream().filter(commandHandleBuilder -> commandHandleBuilder.getName().equals(commandName)).findAny().orElseThrow(() -> new NoSuchCommandException("A subcommand by the name of " + commandName + " was not found"));
        configurator.accept(builder);
        return this;
    }

    List<CommandHandleBuilder> getChildren();

    boolean hasProperty(Class<?> propertyType);

    <T> T getProperty(Class<T> propertyType);

    <T> CommandHandleBuilder putProperty(Class<? super T> type, T property);

    CommandHandleBuilder putProperty(Object property);

    default <T> CommandHandleBuilder applyProperty(Class<? super T> type, T property) {
        BiConsumer<? super T, CommandHandleBuilder> commandModifier = getClientBuilder().getCommandModifier(type);
        if (commandModifier != null)
            commandModifier.accept(property, this);
        return putProperty(type, property);
    }

    default <T> CommandHandleBuilder applyProperty(T property) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) property.getClass();
        BiConsumer<? super T, CommandHandleBuilder> commandModifier = getClientBuilder().getCommandModifier(type);
        if (commandModifier != null)
            commandModifier.accept(property, this);
        return putProperty(type, property);
    }

    default CommandHandleBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        return addPreprocessor(new CommandPreprocessor(identifier, function));
    }

    default CommandHandleBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        return addPreprocessor(new CommandPreprocessor(identifier, predicate));
    }

    default CommandHandleBuilder addPreprocessor(CommandPreprocessor preprocessor) {
        getPreprocessors().add(preprocessor);
        return this;
    }

    CommandHandleBuilder setResultHandler(CommandResultHandler handle);

    /**
     * Sets the delimiter to be used when parsing user input into command parameters.
     *
     * @param splitRegex a Pattern specifying where to split the message content not including the key.
     * @param splitLimit the split limit. For details see {@link Pattern#split(CharSequence, int)}. For default behavior set this value to {@code 0}.
     * @return this
     */
    CommandHandleBuilder setSplitRegex(Pattern splitRegex, int splitLimit);

    /**
     * Sets the delimiter to be used when parsing user input into command parameters.
     *
     * @param splitRegex a String specifying where to split the message content not including the key.
     * @param splitLimit the split limit. For details see {@link Pattern#split(CharSequence, int)}. For default behavior set this value to {@code 0}.
     * @return this
     */
    default CommandHandleBuilder setSplitRegex(String splitRegex, int splitLimit) {
        return setSplitRegex(Pattern.compile(splitRegex), splitLimit);
    }

    List<CommandPreprocessor> getPreprocessors();

    default CommandHandleBuilder sortPreprocessors(Comparator<CommandPreprocessor> comparator) {
        getPreprocessors().sort(comparator);
        return this;
    }

    String getName();

    String[] getKeys();

    String getGroup();

    String getDescription();

    /**
     * This method returns null if the command has been defined with a class or a supplier.
     * If this command was defined with a Consumer or Object, it will return that object.
     *
     * @return the object supplied to create this command.
     */
    Object getDeclaringObject();

    /**
     * This command returns the enclosing class of this command.
     * <ul>
     * <li>If this command was defined by a Consumer, the Class of that consumer is returned.</li>
     * <li>If this command was defined by a Supplier, the Class of the result from that Supplier is returned.</li>
     * <li>If this command was defined by an Object, the Class of that Object is returned.</li>
     * <li>If this command was defined by a Class, then that Class is returned.</li>
     * <li>If this command is a sub-command defined by a Method, then the Class or Inner Class enclosing that Method is returned.</li>
     * </ul>
     *
     * @return a Class.
     */
    Class getDeclaringClass();

    /**
     * Returns the method that is used to invoke this command.
     * If this command was defined with a Consumer, this returns {@code null}.
     * @return a Method
     */
    Method getDeclaringMethod();

    BreadBotBuilder getClientBuilder();


    default CommandHandleBuilder sortPreprocessors() {
        return sortPreprocessors(getClientBuilder().getPriorityComparator());
    }

    /**
     * If set to {@code true}, this command will use the same object to invoke its methods each time it is called. This has no effect on commands built with a consumer or instantiated object.
     *
     * @param isPersistent by default this is false
     * @return this
     */
    CommandHandleBuilder setPersistent(boolean isPersistent);

    /**
     * Sets whether the properties in this command should be retained after building.
     *
     * @param shouldRetainProperties this is by default {@code false}.
     * @return this
     */
    CommandHandleBuilder setRetainProperties(boolean shouldRetainProperties);

    default CommandHandleBuilder configureParameter(int parameterIndex, Consumer<CommandParameterBuilder> configurator) {
        configurator.accept(getParameter(parameterIndex));
        return this;
    }

    CommandParameterBuilder getParameter(int parameterIndex);

    List<CommandParameterBuilder> getParameters();

    @Override
    default CommandHandleBuilder addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(onCommand, configurator);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandClass, configurator);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommand(Class<?> commandClass) {
        CommandHandleBuilderFactory.super.addCommand(commandClass);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandObject, configurator);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommand(Object commandObject) {
        CommandHandleBuilderFactory.super.addCommand(commandObject);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommand(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommand(commandSupplier, configurator);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommand(Supplier<?> commandSupplier) {
        CommandHandleBuilderFactory.super.addCommand(commandSupplier);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommands(packageName, configurator);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommands(String packageName) {
        CommandHandleBuilderFactory.super.addCommands(packageName);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromClasses(Consumer<CommandHandleBuilder> configurator, Class<?>... commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(configurator, commandClasses);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromClasses(Class<?>... commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromObjects(Consumer<CommandHandleBuilder> configurator, Object... commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(configurator, commandObjects);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromObjects(Object... commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromSuppliers(Consumer<CommandHandleBuilder> configurator, Supplier<?>... commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(configurator, commandSuppliers);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromClasses(Collection<Class<?>> commandClasses, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses, configurator);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromClasses(Collection<Class<?>> commandClasses) {
        CommandHandleBuilderFactory.super.addCommandsFromClasses(commandClasses);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromObjects(Collection<?> commandObjects, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects, configurator);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromObjects(Collection<?> commandObjects) {
        CommandHandleBuilderFactory.super.addCommandsFromObjects(commandObjects);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers, configurator);
        return this;
    }

    @Override
    default CommandHandleBuilder addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers) {
        CommandHandleBuilderFactory.super.addCommandsFromSuppliers(commandSuppliers);
        return this;
    }
}