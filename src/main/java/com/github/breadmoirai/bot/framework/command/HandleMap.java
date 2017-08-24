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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandleMap {
    private Map<String, CommandHandle> handles = new HashMap<>();

    public HandleMap(Class<?> commandClass) throws IllegalAccessException, NoSuchMethodException {
        for (Class<?> inner : commandClass.getDeclaredClasses()) {
            final Command innerKey = inner.getAnnotation(Command.class);
            if (innerKey != null) {
                final InnerCommandAdapter innerClassHandle = new InnerCommandAdapter(inner);
                for (String key : innerClassHandle.getKeys()) {
                    handles.put(key, innerClassHandle);
                }
            }
        }

        for (Method method : commandClass.getMethods()) {
            final Command methodKey = method.getAnnotation(Command.class);
            if (methodKey != null) {
                final CommandMethodHandle methodHandle = new CommandMethodHandle(method);
                for (String key : methodHandle.getKeys()) {
                    handles.put(key, methodHandle);
                }
            }
        }
    }

    public String[] getKeys() {
        return handles.keySet().toArray(new String[0]);
    }

    public CommandHandle get(String key) {
        return handles.get(key);
    }
}
