/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.breadbot.framework;

import com.github.breadmoirai.breadbot.framework.error.NoSuchCommandException;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface CommandHandleBuilder extends CommandHandleBuilderFactory<CommandHandleBuilder> {

    CommandHandleBuilder setKeys(String... key);

    CommandHandleBuilder setName(String name);

    CommandHandleBuilder setGroup(String group);

    CommandHandleBuilder setDescription(String description);

    /**
     * Attempts to find a subcommand by the specified name
     *
     * @param commandName the name of the command. By default this is the name of the method or the name of the inner class. If the subcommand is an inner class and its name ends in {@code "command"}, the name only includes the part preceding {@code "command"}.
     * @return The CommandHandleBuilder of the sub command.
     * @throws NoSuchCommandException when the subcommand is not found
     */
    default CommandHandleBuilder getSubCommand(String commandName) {
        return getSubCommands().stream().filter(commandHandleBuilder -> commandHandleBuilder.getName().equals(commandName)).findAny().orElseThrow(() -> new NoSuchCommandException("A subcommand by the name of " + commandName + " was not found"));
    }

    /**
     * Attempts to find a subcommand by the specified name and configure it.
     *
     * @param commandName  the name of the command. By default this is the name of the method or the name of the inner class. If the subcommand is an inner class and its name ends in {@code "command"}, the name only includes the part preceding {@code "command"}.
     * @param configurator a consumer that is used to modify the subcommand specified
     * @return this
     * @throws NoSuchCommandException when the subcommand is not found
     */
    default CommandHandleBuilder configureSubCommand(String commandName, Consumer<CommandHandleBuilder> configurator) {
        CommandHandleBuilder builder = getSubCommands().stream().filter(commandHandleBuilder -> commandHandleBuilder.getName().equals(commandName)).findAny().orElseThrow(() -> new NoSuchCommandException("A subcommand by the name of " + commandName + " was not found"));
        configurator.accept(builder);
        return this;
    }

    List<CommandHandleBuilder> getSubCommands();

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
        @SuppressWarnings("unchecked") Class<T> type = (Class<T>) property.getClass();
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

    List<CommandPreprocessor> getPreprocessors();

    default CommandHandleBuilder sortPreprocessors(Comparator<CommandPreprocessor> comparator) {
        getPreprocessors().sort(comparator);
        return this;
    }


//    default CommandHandleBuilder addAssociatedPreprocessors() {
//        final Map<Class<?>, Function<?, CommandPreprocessor>> preprocessorFactoryMap = getClientBuilder().getPreprocessors().getPreprocessorFactoryMap();
//        final CommandPropertyMapImpl propertyMapBuilder = getPropertyMapBuilder();
//        final Set<Map.Entry<Class<?>, Object>> entries = propertyMapBuilder.entrySet();
//        for (Map.Entry<Class<?>, Object> entry : entries) {
//            final Class<?> propertyType = entry.getKey();
//            final Function<?, CommandPreprocessor> factory = preprocessorFactoryMap.get(propertyType);
//            if (factory != null) {
//                @SuppressWarnings("unchecked") final CommandPreprocessor preprocessor = ((Function<Object, CommandPreprocessor>) factory).apply(entry.getValue());
//                addPreprocessor(preprocessor);
//            }
//        }
//        return this;
//    }

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

    BreadBotClientBuilder getClientBuilder();


    default CommandHandleBuilder sortPreprocessors() {
        return sortPreprocessors(getClientBuilder().getPriorityComparator());
    }

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
}