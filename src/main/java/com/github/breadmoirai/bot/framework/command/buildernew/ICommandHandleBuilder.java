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

import com.github.breadmoirai.bot.framework.command.*;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.Comparator;
import java.util.function.Consumer;

public interface ICommandHandleBuilder {

    ICommandHandleBuilder addCommand(String key, Consumer<CommandEvent> onCommand, Consumer<ICommandHandleBuilder> configurator);

    ICommandHandleBuilder setName(String name);

    ICommandHandleBuilder setGroup(String group);

    ICommandHandleBuilder setDescription(String description);

    ICommandHandleBuilder setPersistence(boolean isPersistent);

    CommandPropertyMapBuilder getPropertyMapBuilder();

    <T> ICommandHandleBuilder putProperty(Class<? super T> type, T property);

    ICommandHandleBuilder putProperty(Object property);

    ICommandHandleBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function);

    ICommandHandleBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate);

    ICommandHandleBuilder addPreprocessors(Iterable<CommandPreprocessor> preprocessors);

    ICommandHandleBuilder sortPerprocessors(Comparator<CommandPreprocessor>preprocessorComparator);

    default ICommandHandleBuilder sortPreprocessors() {
        return sortPerprocessors(CommandPreprocessorsStatic.getPriorityComparator());
    }

    ICommandHandleBuilder addAssociatedPreprocessors();

    CommandHandle build();

}
