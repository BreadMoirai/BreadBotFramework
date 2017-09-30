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
package com.github.breadmoirai.bot.framework.command.impl;

import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.CommandProcessorStack;
import com.github.breadmoirai.bot.framework.command.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class FunctionalCommandImpl implements CommandHandle {

    private final String name;
    private final String[] keys;
    private final List<CommandPreprocessor> preprocessorList;
    private final CommandPropertyMap propertyMap;
    private final Consumer<CommandEvent> function;

    public FunctionalCommandImpl(String name, String[] keys, List<CommandPreprocessor> preprocessorList, CommandPropertyMap propertyMap, Consumer<CommandEvent> function) {
        this.name = name;
        this.keys = keys;
        this.preprocessorList = preprocessorList;
        this.propertyMap = propertyMap;
        this.function = function;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandPropertyMap getPropertyMap() {
        return propertyMap;
    }

    @Override
    public boolean handle(Object parent, CommandEvent event, Iterator<String> keyItr) {
        try {
            final CommandProcessorStack commandPreprocessors = new CommandProcessorStack(parent, this, event, preprocessorList, () -> function.accept(event));
            commandPreprocessors.runNext();
            return commandPreprocessors.result();
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public Collection<CommandHandle> getHandles() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionalCommandImpl that = (FunctionalCommandImpl) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(keys, that.keys)) return false;
        if (propertyMap != null ? !propertyMap.equals(that.propertyMap) : that.propertyMap != null) return false;
        return function != null ? function.equals(that.function) : that.function == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(keys);
        result = 31 * result + (propertyMap != null ? propertyMap.hashCode() : 0);
        result = 31 * result + (function != null ? function.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FunctionalCommandImpl[").append(name).append(']');
        return sb.toString();
    }
}
