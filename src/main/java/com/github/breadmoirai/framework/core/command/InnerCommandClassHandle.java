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
package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.error.DuplicateCommandKeyException;
import com.github.breadmoirai.framework.event.CommandEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InnerCommandClassHandle implements CommandHandle {

    private final String[] keys;
    private final Class<?> klass;
    private final Map<String, CommandHandle> handles;
    private final MethodHandle constructor;

    public InnerCommandClassHandle(Class<?> klass) throws NoSuchMethodException, IllegalAccessException, DuplicateCommandKeyException {
        this.klass = klass;

        constructor = MethodHandles.publicLookup().findConstructor(klass, MethodType.methodType(void.class, klass.getEnclosingClass()));
        handles = new HashMap<>();

        final Class<?>[] inners = klass.getDeclaredClasses();
        for (Class<?> inner : inners) {
            final Command innerKey = inner.getAnnotation(Command.class);
            if (innerKey != null) {
                final CommandClassHandle innerClassHandle = new CommandClassHandle(inner);
                final String[] value = innerKey.value();
                if (value.length == 0) {
                    final String simpleName = inner.getSimpleName().toLowerCase();
                    //todo dup check
                    handles.put(simpleName, innerClassHandle);
                } else {
                    for (String s : value) {
                        //todo dup check
                        handles.put(s, innerClassHandle);
                    }
                }
            }
        }

        for (Method method : klass.getMethods()) {
            final Command methodKey = method.getAnnotation(Command.class);
            if (methodKey != null) {
                final CommandMethodHandle methodHandle = new CommandMethodHandle(method);
                final String[] value = methodKey.value();
                if (value.length == 0) {
                    final String simpleName = method.getName().toLowerCase();
                    //todo dup check
                    handles.put(simpleName, methodHandle);
                } else {
                    for (String s : value) {
                        //todo dup check
                        handles.put(s, methodHandle);
                    }
                }
            }
        }


        final Command key = klass.getAnnotation(Command.class);
        final String[] keyValues = key.value();
        if (keyValues.length == 0) {
            String name = klass.getSimpleName().toLowerCase();
            if (!name.startsWith("command") && name.endsWith("command")) {
                name = name.replace("command", "");
            }
            keys = new String[]{name};
        } else {
            keys = keyValues;
        }
    }

    @Override
    public boolean execute(Object parent, CommandEvent event, int subKey) throws Throwable {
        final Object commandObj = constructor.invoke(parent);
        if (event.getArgumentCount() > subKey) {
            final CommandHandle commandHandle = handles.get(event.getArgumentAt(subKey).getArgument());
            if (commandHandle != null)
                return commandHandle.execute(commandObj, event, subKey + 1);
        }
        final CommandHandle defaultHandle = handles.get("");
        if (defaultHandle != null) return defaultHandle.execute(commandObj, event, subKey + 1);

        return false;
}

    @Override
    public String[] getKeys() {
        return keys;
    }
}
