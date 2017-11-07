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
package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Is a Command. May be a top-level class, an inner class, or a method.
 */
public interface CommandHandle {

    String[] getKeys();

    String getName();

    String getGroup();

    String getDescription();

    CommandPropertyMap getPropertyMap();

    List<CommandPreprocessor> getPreprocessors();

    boolean handle(CommandEvent event);

    default boolean handle(CommandEvent event, Iterator<String> keyItr) {
        return handle(event);
    }

    Map<String, CommandHandle> getSubCommandMap();

    /**
     * This method returns null if the command has been defined with a class or a supplier.
     * If this command was defined with a Consumer or Object, it will return that object.
     *
     * @return the object supplied to create this command.
     */
    Object getDeclaringObject();

    /**
     * If this command is a top-level command
     * then if this command was defined with a Class, that class is returned.
     * Else if this command was defined with a Supplier, then the class of the result of that Supplier is returned.
     * Else if this command was defined with an Object or Consumer, then the class of that Object is returned.
     *
     * If this command is not a top-level command
     * @return
     */
    Class getDeclaringClass();

    Method getDeclaringMethod();

}