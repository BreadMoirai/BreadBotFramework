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
package com.github.breadmoirai.bot.framework.command.builder;

import com.github.breadmoirai.bot.framework.command.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandHandleBuilder {

    private String name;
    String[] keys;
    private CommandPropertyMapBuilder propertyBuilder;
    private List<CommandPreprocessor> preprocessorList;

    public CommandHandleBuilder(String name, CommandPropertyMap map, Annotation[] annotations) {
        this.name = name;
        propertyBuilder = new CommandPropertyMapBuilder(map);
        preprocessorList = new ArrayList<>();
        propertyBuilder.putAnnotations(annotations);
    }

    public String getName() {
        return name;
    }

    public CommandHandleBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CommandHandleBuilder setKeys(String... keys) {
        this.keys = keys;
        return this;
    }

    public String[] getKeys() {
        return keys;
    }

    public abstract CommandHandle build();

    public CommandPropertyMapBuilder getPropertyBuilder() {
        return propertyBuilder;
    }

    public boolean hasProperty(Class<?> propertyType) {
        return getPropertyBuilder().containsProperty(propertyType);
    }

    public <T> T getProperty(Class<T> propertyType) {
        return getPropertyBuilder().getProperty(propertyType);
    }

    public CommandHandleBuilder putProperty(Object property) {
        getPropertyBuilder().putProperty(property);
        return this;
    }

    public <T> CommandHandleBuilder putProperty(Class<? super T> type, T property) {
        getPropertyBuilder().putProperty(type, property);
        return this;
    }

    public CommandHandleBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        preprocessorList.add(new CommandPreprocessor(identifier, function));
        return this;
    }

    public CommandHandleBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        preprocessorList.add(new CommandPreprocessor(identifier, predicate));
        return this;
    }

    public CommandHandleBuilder addPreprocessors(Iterable<CommandPreprocessor> preprocessors) {
        for (CommandPreprocessor preprocessor : preprocessors) {
            preprocessorList.add(preprocessor);
        }
        return this;
    }

    /**
     * Returns a modifiable list of the preprocessors
     *
     * @return a list
     */
    public List<CommandPreprocessor> getPreprocessorList() {
        return preprocessorList;
    }

    public CommandHandleBuilder sortPreprocessors() {
        getPreprocessorList().sort(CommandPreprocessorsStatic.getPriorityComparator());
        return this;
    }

    /**
     * Iterates through the currently registered properties and adds associated preprocessors
     */
    public CommandHandleBuilder addAssociatedPreprocessors() {
        CommandPreprocessorsStatic.addPrepocessors(this);
        return this;
    }
}