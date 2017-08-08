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

import com.github.breadmoirai.framework.event.CommandEvent;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.stream.Stream;

public class CommandClassHandle implements CommandHandle {

    private final boolean keyless;
    private final Class<?> klass;
    private final String[] keys;
    private final Map<String, CommandHandle> handles;
    private final Constructor<?> constructor;

    public CommandClassHandle(Class<?> klass) throws NoSuchMethodException {
        this.klass = klass;
        final Command key = klass.getAnnotation(Command.class);
        keyless = key != null;
        final Class<?>[] inners = klass.getDeclaredClasses();
        final Class<?> enclosingClass = klass.getEnclosingClass();
        if (enclosingClass == null)
            constructor = klass.getConstructor();
        else constructor = klass.getConstructor(enclosingClass);

    }

    @Override
    public String[] getKeys() {
        return new String[0];
    }

    @Override
    public boolean execute(Object parent, CommandEvent event, String subKey) throws Throwable {
        return false;
    }

    @Override
    public Stream<CommandHandle> getHandles() {
        return handles.stream();
    }
}
