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

import com.github.breadmoirai.bot.framework.command.*;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParser;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandProcessStack;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CommandMethodImpl implements CommandHandle {
    private final Method method;
    private final CommandParameter[] commandParameters;
    private final MethodHandle handle;
    private final CommandPropertyMap properties;
    private final List<CommandPreprocessor> preprocessorList;
    private final String[] keys;

    public CommandMethodImpl(Method method, CommandParameter[] commandParameters, CommandPropertyMap properties, List<CommandPreprocessor> preprocessorList, String[] keys) throws IllegalAccessException {
        this.method = method;
        this.commandParameters = commandParameters;
        handle = MethodHandles.lookup().unreflect(method);
        this.properties = properties;
        this.preprocessorList = preprocessorList;
        this.keys = keys;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public CommandPropertyMap getPropertyMap() {
        return properties;
    }

    @Override
    public boolean handle(Object parent, CommandEvent event, Iterator<String> keyItr) {
        final CommandProcessStack commandPreprocessors = new CommandProcessStack(parent, this, event, preprocessorList, () -> parseAndInvokeHandle(parent, event));
        commandPreprocessors.runNext();
        return commandPreprocessors.result();
    }

    private boolean parseAndInvokeHandle(Object parent, CommandEvent event) {
        final CommandParser parser = new CommandParser(event, this, event.getArguments(), commandParameters);
        while (parser.hasNext()) {
            parser.mapNext();
        }
        if (!parser.hasFailed()) {
            try {
                handle.asSpreader(Object[].class, commandParameters.length).invoke(parent, parser.getResults());
                return true;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return false;
            }
        }
        return false;
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
    public List<CommandPreprocessor> getPreprocessors() {
        return preprocessorList;
    }
}
