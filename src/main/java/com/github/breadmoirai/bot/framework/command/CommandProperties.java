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
package com.github.breadmoirai.bot.framework.command;

import com.github.breadmoirai.bot.framework.command.impl.CommandParameterBuilder;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CommandProperties {

    private List<String> preprocessorPriorityList = Collections.emptyList();

    private final Map<Class<?>, BiConsumer<?, CommandHandleBuilder>> commandPropertyMap = new HashMap<>();
    private final Map<Class<?>, BiConsumer<?, CommandParameterBuilder>> parameterPropertyMap = new HashMap<>();

    public CommandProperties() {
    }

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier it is overridden.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link java.util.function.BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    public <T> void putCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        commandPropertyMap.put(propertyType, configurator);
    }

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier, the passed BiConsumer is appended to the end of it so that it is applied to the builder after the existing one has been applied.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link java.util.function.BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    public <T> void appendCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        //noinspection unchecked
        commandPropertyMap.merge(propertyType, configurator, BiConsumer::andThen);
    }

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier it is overridden.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link java.util.function.BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    public <T> void putParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        parameterPropertyMap.put(propertyType, configurator);
    }

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier, the passed BiConsumer is appended to the end of it so that it is applied to the builder after the existing one has been applied.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link java.util.function.BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    public <T> void appendParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        //noinspection unchecked
        parameterPropertyMap.merge(propertyType, configurator, BiConsumer::andThen);
    }

    /**
     * Applies modifiers to a CommandHandleBuilder based on whether the handle contains a property.
     *
     * @param builder the builder to modify
     */
    public void applyModifiers(CommandHandleBuilder builder) {
        for (Class<?> aClass : commandPropertyMap.keySet()) {
            applyCommandModifier(aClass, builder);
        }
    }

    /**
     * Retrieves a BiConsumer that is used to modify a CommandHandleBuilder.
     *
     * @param propertyType the class of the Property.
     * @param <T>          the property type.
     * @return a BiConsumer if present, otherwise {@code null}.
     * @see #putCommandModifier(Class, BiConsumer)
     * @see #appendCommandModifier(Class, BiConsumer)
     * @see #applyCommandModifier(Class, CommandHandleBuilder)
     */
    public <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        BiConsumer<?, CommandHandleBuilder> biConsumer = commandPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandHandleBuilder> consumer = (BiConsumer<T, CommandHandleBuilder>) biConsumer;
        return consumer;
    }

    /**
     * Applies a modifier that is associated with a certain {@code propertyType} to the passed {@code builder}.
     * If a modifier is not found the {@code builder} is not modified.
     *
     * @param propertyType the property class
     * @param builder the CommandHandleBuilder to be modified
     * @param <T> the property type
     * @see #putCommandModifier(Class, BiConsumer)
     * @see #appendCommandModifier(Class, BiConsumer)
     * @see #getCommandModifier(Class)
     */
    public <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        BiConsumer<T, CommandHandleBuilder> commandModifier = getCommandModifier(propertyType);
        T property = builder.getProperty(propertyType);
        commandModifier.accept(property, builder);
    }

    /**
     * Applies modifiers to a CommandParameterBuilder based on whether the handle contains a property.
     *
     * @param builder the builder to modify
     */
    public void applyModifiers(CommandParameterBuilder builder) {
        for (Class<?> aClass : parameterPropertyMap.keySet()) {
            applyParameterModifier(aClass, builder);
        }
    }

    /**
     * Retrieves a BiConsumer that is used to modify a CommandParameterBuilder.
     *
     * @param propertyType the class of the Property.
     * @param <T>          the property type.
     * @return a BiConsumer if present, otherwise {@code null}.
     * @see #putParameterModifier(Class, BiConsumer)
     * @see #appendParameterModifier(Class, BiConsumer)
     * @see #applyParameterModifier(Class, CommandParameterBuilder)
     */
    public <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        BiConsumer<?, CommandParameterBuilder> biConsumer = parameterPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandParameterBuilder> consumer = (BiConsumer<T, CommandParameterBuilder>) biConsumer;
        return consumer;
    }

    /**
     * Applies a modifier that is associated with a certain {@code propertyType} to the passed {@code builder}.
     * If a modifier is not found the {@code builder} is not modified.
     *
     * @param propertyType the property class
     * @param builder the CommandHandleBuilder to be modified
     * @param <T> the property type
     * @see #putParameterModifier(Class, BiConsumer)
     * @see #appendParameterModifier(Class, BiConsumer)
     * @see #getParameterModifier(Class)
     */
    public <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        BiConsumer<T, CommandParameterBuilder> commandModifier = getParameterModifier(propertyType);
        T property = builder.getProperty(propertyType);
        commandModifier.accept(property, builder);
    }


    private <T> void associatePreprocessor(Class<T> propertyType, Function<T, CommandPreprocessor> factory) {
        appendCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(factory.apply(t)));
    }

    /**
     * Remind me to write docs for this.
     *
     * @param identifier
     * @param propertyType
     * @param factory
     * @param <T>
     */
    public <T> void associatePreprocessorFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorFunction> factory) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, factory.apply(o)));
    }

    /**
     * @param identifier
     * @param propertyType
     * @param factory
     * @param <T>
     */
    public <T> void associatePreprocessorPredicateFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorPredicate> factory) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, factory.apply(o)));
    }

    /**
     * @param identifier
     * @param propertyType
     * @param function
     * @param <T>
     */
    public <T> void associatePreprocessor(String identifier, Class<T> propertyType, CommandPreprocessorFunction function) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, function));
    }

    /**
     * @param identifier
     * @param propertyType
     * @param predicate
     */
    public void associatePreprocessorPredicate(String identifier, Class<?> propertyType, CommandPreprocessorPredicate predicate) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, predicate));
    }

    public List<String> getPreprocessorPriorityList() {
        return preprocessorPriorityList;
    }


    public void setPreprocessorPriority(String... identifiers) {
        preprocessorPriorityList = Arrays.asList(identifiers);
    }

    public void setPreprocessorPriority(List<String> identifierList) {
        preprocessorPriorityList = identifierList;
    }

    private int getPriority(String identifier, List<String> list) {
        final int i = list.indexOf(identifier);
        if (i != -1) return i;
        final int j = list.indexOf(null);
        if (j != -1) return j;
        else return list.size();
    }

    public Comparator<CommandPreprocessor> getPriorityComparator() {
        return new PriorityComparator(preprocessorPriorityList);
    }

    public Comparator<CommandPreprocessor> getPreprocessorComparator(String... identifier) {
        return new PriorityComparator(Arrays.asList(identifier));
    }

    public class PriorityComparator implements Comparator<CommandPreprocessor> {

        private final List<String> identifierList;

        public PriorityComparator(List<String> identifierList) {
            this.identifierList = identifierList;
        }

        @Override
        public int compare(CommandPreprocessor o1, CommandPreprocessor o2) {
            return getPriority(o1.getIdentifier(), identifierList) - getPriority(o2.getIdentifier(), identifierList);
        }
    }
}
