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

import com.github.breadmoirai.bot.framework.command.builder.CommandHandleBuilder;

import java.util.*;
import java.util.function.Function;

public class CommandPreprocessors {

    /**
     * private constructor for a static class. should probably de-staticfy or make singleton at the very least.
     */
    private CommandPreprocessors() {
    }

    private static List<String> preprocessorPriorityList = Collections.emptyList();
    private static final Map<Class<?>, Function<?, CommandPreprocessor>> preprocessorMap = new HashMap<>();

    public static <T> void associatePreprocessor(Class<T> propertyType, Function<T, CommandPreprocessor> function) {
        preprocessorMap.put(propertyType, function);
    }

    public static <T> void associatePreprocessorFunctionFactory(Class<T> propertyType, String identifier, Function<T, CommandPreprocessorFunction> factory) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, factory.apply(o)));
    }

    public static <T> void associatePreprocessorPredicateFactory(Class<T> propertyType, String identifier, Function<T, CommandPreprocessorPredicate> factory) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, factory.apply(o)));
    }

    public static <T> void associatePreprocessorFunction(Class<T> propertyType, String identifier, CommandPreprocessorFunction function) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, function));
    }

    public static void associatePreprocessorPredicate(Class<?> propertyType, String identifier, CommandPreprocessorPredicate predicate) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, predicate));
    }

    public static <T> CommandPreprocessor getPreprocessor(T obj) {
        @SuppressWarnings("unchecked") final Function<T, CommandPreprocessor> commandPreprocessorFunction = (Function<T, CommandPreprocessor>) preprocessorMap.get(obj.getClass());
        if (commandPreprocessorFunction == null) return null;
        return commandPreprocessorFunction.apply(obj);
    }

    public static <T> CommandPreprocessor getPreprocessor(Class<T> type) {
        @SuppressWarnings("unchecked") final Function<T, CommandPreprocessor> commandPreprocessorFunction = (Function<T, CommandPreprocessor>) preprocessorMap.get(type);
        if (commandPreprocessorFunction == null) return null;
        return commandPreprocessorFunction.apply(null);
    }

    public static int getPreprocessorPriority(String identifier) {
        return getPriority(identifier, preprocessorPriorityList);
    }

    public static void setPreprocessorPriority(String... identifiers) {
        preprocessorPriorityList = Arrays.asList(identifiers);
    }

    public static void setPreprocessorPriorityList(List<String> identifierList) {
        preprocessorPriorityList = identifierList;
    }

    /**
     * Will add associated preprocessors to the passed {@link CommandHandleBuilder} according to it's properties sorted by it's identifier priority as set in {@code #setPreprocessorPriority}
     *
     * @param handleBuilder A CommandHandleBuilder of a top-level class, an inner class, or a method.
     */
    public static void addPrepocessors(CommandHandleBuilder handleBuilder) {
        final List<CommandPreprocessor> preprocessors = new ArrayList<>();
        for (Object o : handleBuilder.getPropertyBuilder()) {
            final CommandPreprocessor preprocessor = getPreprocessor(o);
            if (preprocessor != null)
                preprocessors.add(preprocessor);
        }
        preprocessors.sort(getPriorityComparator());
        handleBuilder.addPreprocessors(preprocessors);
    }

    private static int getPriority(String identifier, List<String> list) {
        final int i = list.indexOf(identifier);
        if (i != -1) return i;
        final int j = list.indexOf(null);
        if (j != -1) return j;
        else return list.size();
    }

    public static Comparator<CommandPreprocessor> getPriorityComparator() {
        return new PriorityComparator(preprocessorPriorityList);
    }

    public static Comparator<CommandPreprocessor> getComparator(String... identifier) {
        return new PriorityComparator(Arrays.asList(identifier));
    }

    public static class PriorityComparator implements Comparator<CommandPreprocessor> {

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
