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
package com.github.breadmoirai.bot.framework.command.preprocessor;

import com.github.breadmoirai.bot.framework.command.builder.CommandHandleBuilder;
import com.github.breadmoirai.bot.framework.command.buildernew.CommandParameterBuilder;
import com.github.breadmoirai.bot.util.TriConsumer;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;

public class CommandProperties {

    private List<String> preprocessorPriorityList = Collections.emptyList();

    private final Map<Class<?>, TriConsumer<?, Object, CommandHandleBuilder>> commandPropertyMap = new HashMap<>();
    private final Map<Class<?>, TriConsumer<?, Parameter, CommandParameterBuilder>> parameterPropertyMap = new HashMap<>();

    public CommandProperties() {
    }

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link com.github.breadmoirai.bot.util.TriConsumer TriConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the object that the property is attached to.
     *                     This is either a {@link java.lang.Class} or a {@link java.lang.reflect.Method Method}.
     *                     The third argument is the {@link com.github.breadmoirai.bot.framework.command.builder.CommandHandleBuilder CommandHandleBuilder} that is intended to be modified by this TriConsumer.
     * @param <T>          the propertyType
     */
    public <T> void setCommandApplication(Class<T> propertyType, TriConsumer<T, Object, CommandHandleBuilder> configurator) {
        propertyMap.put(propertyType, configurator);
    }

    public <T> void append(Class<T> propertyType, TriConsumer<T, Object, CommandHandleBuilder> configurator) {
        //noinspection unchecked
        propertyMap.merge(propertyType, configurator, TriConsumer::andThen);
    }

    public <T> TriConsumer<T, ?, CommandHandleBuilder> getPropertyConfigurator(Class<? super T> type) {
        return
    }

    private <T> void associatePreprocessor(Class<T> propertyType, Function<T, CommandPreprocessor> factory) {
        preprocessorFactoryMap.put(propertyType, factory);
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
