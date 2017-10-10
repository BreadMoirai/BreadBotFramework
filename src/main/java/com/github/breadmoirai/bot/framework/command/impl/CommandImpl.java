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
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandProcessorStack;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CommandImpl implements CommandHandle {

    private static final Logger log = LoggerFactory.getLogger("Command");

    private final String name;
    private final String[] keys;
    private final Supplier<Object> supplier;
    private final Map<String, CommandHandle> handleMap;
    private final CommandPropertyMap propertyMap;
    private final List<CommandPreprocessor> preprocessorList;

    public CommandImpl(String name, String[] keys, Supplier<Object> supplier, Map<String, CommandHandle> handleMap, CommandPropertyMap propertyMap, List<CommandPreprocessor> preprocessorList) {
        this.name = name;
        this.keys = keys;
        this.supplier = supplier;
        this.handleMap = handleMap;
        this.propertyMap = propertyMap;
        this.preprocessorList = preprocessorList;
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
        final Object commandObj = supplier.get();
        if (commandObj == null) return false;
        final CommandHandle targetHandle;
        if (keys == null) {
            targetHandle = handleMap.get(event.getKey().toLowerCase());
        } else {
            final CommandHandle commandHandle;
            if (keyItr.hasNext() && (commandHandle = handleMap.get(keyItr.next().toLowerCase())) != null) {
                targetHandle = commandHandle;
            } else {
                final CommandHandle defaultHandle = handleMap.get("");
                if (defaultHandle != null) {
                    targetHandle = defaultHandle;
                } else {
                    targetHandle = null;
                }
            }
        }
        if (targetHandle != null) {
            final Runnable executeHandle = () -> targetHandle.handle(commandObj, event, keyItr);
            new CommandProcessorStack(commandObj, targetHandle, event, preprocessorList, executeHandle).runNext();
            return true;
        }
        return false;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public String[] getEffectiveKeys() {
        return keys == null ? handleMap.keySet().toArray(new String[0]) : keys;
    }

    @Override
    public Collection<CommandHandle> getHandles() {
        return handleMap.values();
    }

    @Override
    public String toString() {
        return "CommandImpl[" + name + "]";
    }

    @Override
    public List<CommandPreprocessor> getPreprocessors() {
        return preprocessorList;
    }
}
