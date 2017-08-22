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

import com.github.breadmoirai.bot.framework.error.DuplicateCommandKeyException;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

public class CommandAdapter extends CommandWrapper implements CommandHandle {

    private final HandleMap handleMap;
    private final Supplier<Object> supplier;
    private final boolean hasClassKey;

    private final String toString;

    public CommandAdapter(Class<?> commandClass) throws NoSuchMethodException, IllegalAccessException, DuplicateCommandKeyException {
        super(commandClass);
        final MethodHandle constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class));
        supplier = () -> {
            try {
                return constructor.invoke();
            } catch (Throwable throwable) {
                Commands.LOG.fatal(throwable);
                return null;
            }
        };

        handleMap = new HandleMap(commandClass);

        hasClassKey = commandClass.isAnnotationPresent(Command.class);

        toString = String.format("CommandAdapter[%s]", commandClass.getSimpleName());
    }

    public CommandAdapter(Object commandObject) throws NoSuchMethodException, IllegalAccessException, DuplicateCommandKeyException {
        super(commandObject.getClass());
        supplier = () -> commandObject;

        final Class<?> commandClass = commandObject.getClass();

        handleMap = new HandleMap(commandClass);

        hasClassKey = commandClass.isAnnotationPresent(Command.class);

        toString = String.format("CommandAdapter[%s]", commandClass.getSimpleName());


    }

    @Override
    public boolean execute(Object parent, CommandEvent event, int subKey) throws Throwable {
        final Object commandObj = supplier.get();
        if (commandObj == null) return false;
        if (hasClassKey) {
            return handleMap.get(event.getKey().toLowerCase()).execute(commandObj, event, 0);
        } else {
            if (event.getArgumentCount() >= 1) {
                final CommandHandle commandHandle = handleMap.get(event.getArgumentAt(0).getArgument());
                if (commandHandle != null)
                    return commandHandle.execute(commandObj, event, 1);
            }
            final CommandHandle defaultHandle = handleMap.get("");
            if (defaultHandle != null) return defaultHandle.execute(commandObj, event, 1);
        }
        return false;
    }

    @Override
    public String[] getKeys() {
        if (hasClassKey) {
            return super.getKeys();
        } else {
            return handleMap.getKeys();
        }
    }

    @Override
    public void handle(CommandEvent event) throws Throwable {
        execute(event);
    }

    @Override
    public String toString() {
        return toString;
    }
}
