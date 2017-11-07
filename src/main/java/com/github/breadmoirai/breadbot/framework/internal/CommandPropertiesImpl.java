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
package com.github.breadmoirai.breadbot.framework.internal;

import com.github.breadmoirai.breadbot.framework.CommandProperties;
import com.github.breadmoirai.breadbot.framework.command.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.command.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorPredicate;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CommandPropertiesImpl implements CommandProperties {

    private List<String> preprocessorPriorityList = Collections.emptyList();

    private final Map<Class<?>, BiConsumer<?, CommandHandleBuilder>> commandPropertyMap = new HashMap<>();
    private final Map<Class<?>, BiConsumer<?, CommandParameterBuilder>> parameterPropertyMap = new HashMap<>();

    public CommandPropertiesImpl() {
        new DefaultCommandProperties().initialize(this);
    }

    @Override
    public <T> void putCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        commandPropertyMap.put(propertyType, configurator);
    }

    @Override
    public <T> void appendCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        commandPropertyMap.merge(propertyType, configurator, (c1, c2) -> {
            @SuppressWarnings("unchecked") BiConsumer<Object, CommandHandleBuilder> cc1 = (BiConsumer<Object, CommandHandleBuilder>) c1;
            @SuppressWarnings("unchecked") BiConsumer<Object, CommandHandleBuilder> cc2 = (BiConsumer<Object, CommandHandleBuilder>) c2;
            return cc1.andThen(cc2);
        });
    }

    @Override
    public <T> void putParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        parameterPropertyMap.put(propertyType, configurator);
    }

    @Override
    public <T> void appendParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        parameterPropertyMap.merge(propertyType, configurator, (c1, c2) -> {
            @SuppressWarnings("unchecked") BiConsumer<Object, CommandParameterBuilder> cc1 = (BiConsumer<Object, CommandParameterBuilder>) c1;
            @SuppressWarnings("unchecked") BiConsumer<Object, CommandParameterBuilder> cc2 = (BiConsumer<Object, CommandParameterBuilder>) c2;
            return cc1.andThen(cc2);
        });
    }

    @Override
    public void applyModifiers(CommandHandleBuilder builder) {
        for (Class<?> aClass : commandPropertyMap.keySet()) {
            if (builder.hasProperty(aClass))
                applyCommandModifier(aClass, builder);
        }
    }

    @Override
    public <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        BiConsumer<?, CommandHandleBuilder> biConsumer = commandPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandHandleBuilder> consumer = (BiConsumer<T, CommandHandleBuilder>) biConsumer;
        return consumer;
    }

    @Override
    public <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        BiConsumer<T, CommandHandleBuilder> commandModifier = getCommandModifier(propertyType);
        T property = builder.getProperty(propertyType);
        commandModifier.accept(property, builder);
    }

    @Override
    public void applyModifiers(CommandParameterBuilder builder) {
        for (Class<?> aClass : parameterPropertyMap.keySet()) {
            if (builder.hasProperty(aClass))
            applyParameterModifier(aClass, builder);
        }
    }

    @Override
    public <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        BiConsumer<?, CommandParameterBuilder> biConsumer = parameterPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandParameterBuilder> consumer = (BiConsumer<T, CommandParameterBuilder>) biConsumer;
        return consumer;
    }

    @Override
    public <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        BiConsumer<T, CommandParameterBuilder> commandModifier = getParameterModifier(propertyType);
        T property = builder.getProperty(propertyType);
        commandModifier.accept(property, builder);
    }


    private <T> void associatePreprocessor(Class<T> propertyType, Function<T, CommandPreprocessor> factory) {
        appendCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(factory.apply(t)));
    }

    @Override
    public <T> void associatePreprocessorFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorFunction> factory) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, factory.apply(o)));
    }

    @Override
    public <T> void associatePreprocessorPredicateFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorPredicate> factory) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, factory.apply(o)));
    }

    @Override
    public void associatePreprocessor(String identifier, Class<?> propertyType, CommandPreprocessorFunction function) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, function));
    }

    @Override
    public void associatePreprocessorPredicate(String identifier, Class<?> propertyType, CommandPreprocessorPredicate predicate) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, predicate));
    }

    @Override
    public List<String> getPreprocessorPriorityList() {
        return preprocessorPriorityList;
    }


    @Override
    public void setPreprocessorPriority(String... identifiers) {
        preprocessorPriorityList = Arrays.asList(identifiers);
    }

    @Override
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

    @Override
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
