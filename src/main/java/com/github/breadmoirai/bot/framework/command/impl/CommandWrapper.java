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

import com.github.breadmoirai.bot.framework.command.Command;
import com.github.breadmoirai.bot.framework.command.ICommand;

public abstract class CommandWrapper implements ICommand {

    private final String[] keys;


    public CommandWrapper(Class<?> commandClass) {
        final Command key = commandClass.getAnnotation(Command.class);
        if (key == null || key.value().length == 0) {
            String name = commandClass.getSimpleName().toLowerCase();
            if (!name.startsWith("command") && name.endsWith("command")) {
                name = name.replace("command", "");
            }
            keys = new String[]{name};
        } else {
            keys = key.value();
        }
    }

    public String[] getKeys() {
        return keys;
    }
}
