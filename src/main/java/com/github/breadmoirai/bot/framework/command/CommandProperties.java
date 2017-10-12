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
import com.github.breadmoirai.bot.util.TriConsumer;

import java.lang.reflect.Parameter;
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

    public void applyModifiers(CommandHandleBuilder builder) {
        for (Class<?> aClass : commandPropertyMap.keySet()) {
            applyCommandModifier(aClass, builder);
        }
    }

    private <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        BiConsumer<?, CommandHandleBuilder> biConsumer = commandPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandHandleBuilder> consumer = (BiConsumer<T, CommandHandleBuilder>) biConsumer;
        return consumer;
    }

    private <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        BiConsumer<T, CommandHandleBuilder> commandModifier = getCommandModifier(propertyType);
        T property = builder.getProperty(propertyType);
        commandModifier.accept(property, builder);
    }

    public void applyModifiers(CommandParameterBuilder builder) {
        for (Class<?> aClass : parameterPropertyMap.keySet()) {
            applyParameterModifier(aClass, builder);
        }
    }

    private <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        BiConsumer<?, CommandParameterBuilder> biConsumer = parameterPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandParameterBuilder> consumer = (BiConsumer<T, CommandParameterBuilder>) biConsumer;
        return consumer;
    }

    private <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
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

    /**
     * @param propertyObj
     * @param <T>
     * @return
     */
    public <T> CommandPreprocessor getAssociatedPreprocessor(T propertyObj) {
        @SuppressWarnings("unchecked") final Function<T, CommandPreprocessor> commandPreprocessorFunction = (Function<T, CommandPreprocessor>) preprocessorFactoryMap.get(propertyObj.getClass());
        if (commandPreprocessorFunction == null) return null;
        return commandPreprocessorFunction.apply(propertyObj);
    }

    /**
     * @param type
     * @param <T>
     * @return
     */
    public <T> CommandPreprocessor getAssociatedPreprocessor(Class<T> type) {
        @SuppressWarnings("unchecked") final Function<T, CommandPreprocessor> commandPreprocessorFunction = (Function<T, CommandPreprocessor>) preprocessorFactoryMap.get(type);
        if (commandPreprocessorFunction == null) return null;
        return commandPreprocessorFunction.apply(null);
    }

    public List<String> getPreprocessorPriorityList() {
        return preprocessorPriorityList;
    }

    public Map<Class<?>, Function<?, CommandPreprocessor>> getPreprocessorFactoryMap() {
        return preprocessorFactoryMap;
    }

    public List<CommandPreprocessor> getPreprocessorList() {
        return preprocessorList;
    }

    /**
     * @param identifier
     * @return
     */
    public CommandPreprocessor getPreprocessor(String identifier) {
        return preprocessorList.stream().filter(preprocessor -> preprocessor.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public void setPreprocessorPriority(String... identifiers) {
        preprocessorPriorityList = Arrays.asList(identifiers);
    }

    public void setPreprocessorPriority(List<String> identifierList) {
        preprocessorPriorityList = identifierList;
    }

    /**
     * Will add associated preprocessors to the passed {@link CommandHandleBuilder} according to it's properties sorted by it's identifier priority as set in {@code #setPreprocessorPriority}
     *
     * @param handleBuilder A CommandHandleBuilder of a top-level class, an inner class, or a method.
     */
    public void addPreprocessors(CommandHandleBuilder handleBuilder) {
        final List<CommandPreprocessor> preprocessors = new ArrayList<>();
        for (Object o : handleBuilder.getPropertyBuilder()) {
            final CommandPreprocessor preprocessor = getAssociatedPreprocessor(o);
            if (preprocessor != null)
                preprocessors.add(preprocessor);
        }
        preprocessors.sort(getPriorityComparator());
        handleBuilder.addPreprocessors(preprocessors);
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
