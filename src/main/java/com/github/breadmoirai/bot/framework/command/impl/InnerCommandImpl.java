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

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InnerCommandImpl implements CommandHandle {

    private static final Logger log = LoggerFactory.getLogger("CommandEngine");

    private final String name;
    private final String[] keys;
    private final MethodHandle constructor;
    private final Map<String, CommandHandle> handleMap;
    private final CommandPropertyMap propertyMap;
    private final List<CommandPreprocessor> preprocessorList;

    public InnerCommandImpl(String name, String[] keys, MethodHandle constructor, Map<String, CommandHandle> handleMap, CommandPropertyMap propertyMap, List<CommandPreprocessor> preprocessorList) {
        this.name = name;
        this.keys = keys;
        this.constructor = constructor;
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
    public boolean handle(Object parent, CommandEvent event, Iterator<String> keyIterator) {
        final Object commandObj;
        try {
            commandObj = constructor.invoke(parent);
        } catch (Throwable throwable) {
            log.error("An error occurred while attempting to instantiate an inner command object.", throwable);
            return false;
        }
        if (keyIterator.hasNext()) {
            final String next = keyIterator.next().toLowerCase();
            final CommandHandle commandHandle = handleMap.get(next);
            if (commandHandle != null) {
                final CommandProcessorStack commandPreprocessors = new CommandProcessorStack(commandObj, commandHandle, event, preprocessorList, () -> commandHandle.handle(commandObj, event, keyIterator));
                commandPreprocessors.runNext();
                return commandPreprocessors.result();
            }
        }
        final CommandHandle defaultHandle = handleMap.get("");
        if (defaultHandle != null) {
            final CommandProcessorStack commandPreprocessors = new CommandProcessorStack(commandObj, defaultHandle, event, preprocessorList, () -> defaultHandle.handle(commandObj, event, null));
            commandPreprocessors.runNext();
            return commandPreprocessors.result();
        }
        return false;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public Collection<CommandHandle> getHandles() {
        return handleMap.values();
    }

    @Override
    public List<CommandPreprocessor> getPreprocessors() {
        return preprocessorList;
    }
}
