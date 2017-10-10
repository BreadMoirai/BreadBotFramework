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
package com.github.breadmoirai.bot.framework.command.buildernew;

import com.github.breadmoirai.bot.framework.BreadBotClient;
import com.github.breadmoirai.bot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessorFunction;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessorPredicate;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMapBuilder;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CommandHandleBuilder {

    /**
     * Adds a subCommand
     * @param key
     * @param onCommand
     * @param configurator
     * @return
     */
    CommandHandleBuilder addSubCommand(String[] key, Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator);

    default CommandHandleBuilder addSubCommand(String key, Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        return addSubCommand(new String[]{key}, onCommand, configurator);
    }

    CommandHandleBuilder setKeys(String... key);

    CommandHandleBuilder setName(String name);

    CommandHandleBuilder setGroup(String group);

    CommandHandleBuilder setDescription(String description);

    CommandPropertyMapBuilder getPropertyMapBuilder();

    <T> CommandHandleBuilder putProperty(Class<? super T> type, T property);

    CommandHandleBuilder putProperty(Object property);

    default CommandHandleBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        return addPreprocessor(new CommandPreprocessor(identifier, function));
    }

    default CommandHandleBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        return addPreprocessor(new CommandPreprocessor(identifier, predicate));
    }

    CommandHandleBuilder addPreprocessor(CommandPreprocessor preprocessor);

    CommandHandleBuilder sortPreprocessors(Comparator<CommandPreprocessor>preprocessorComparator);

    default CommandHandleBuilder sortPreprocessors(String... identifierPriority) {
        return sortPreprocessors(getClientBuilder().getPreprocessorComparator(identifierPriority));
    }

    default CommandHandleBuilder sortPreprocessors() {
        return sortPreprocessors(getClientBuilder().getPriorityComparator());
    }

    default CommandHandleBuilder addAssociatedPreprocessors() {
        final Map<Class<?>, Function<?, CommandPreprocessor>> preprocessorFactoryMap = getClientBuilder().getPreprocessors().getPreprocessorFactoryMap();
        final CommandPropertyMapBuilder propertyMapBuilder = getPropertyMapBuilder();
        final Set<Map.Entry<Class<?>, Object>> entries = propertyMapBuilder.entrySet();
        for (Map.Entry<Class<?>, Object> entry : entries) {
            final Class<?> propertyType = entry.getKey();
            final Function<?, CommandPreprocessor> factory = preprocessorFactoryMap.get(propertyType);
            if (factory != null) {
                @SuppressWarnings("unchecked") final CommandPreprocessor preprocessor = ((Function<Object, CommandPreprocessor>) factory).apply(entry.getValue());
                addPreprocessor(preprocessor);
            }
        }
        return this;
    }

    BreadBotClientBuilder getClientBuilder();

    CommandHandle build(BreadBotClient client);

}
