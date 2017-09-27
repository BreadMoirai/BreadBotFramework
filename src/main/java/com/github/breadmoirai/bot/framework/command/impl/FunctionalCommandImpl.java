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
import com.github.breadmoirai.bot.framework.command.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.Iterator;
import java.util.function.Consumer;

public class FunctionalCommandImpl implements CommandHandle {

    public String name;
    public String[] keys;
    private CommandPropertyMap propertyMap;
    Consumer<CommandEvent> function;

    public FunctionalCommandImpl(String name, String[] keys, CommandPropertyMap propertyMap, Consumer<CommandEvent> function) {
        this.name = name;
        this.keys = keys;
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
            function.accept(event);
            return true;
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
    public String[] getHandleKeys() {
        return null;
    }
}
