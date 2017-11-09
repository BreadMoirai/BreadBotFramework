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

import com.github.breadmoirai.breadbot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is used to invoke a command
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
     * This command returns the enclosing class of this command.
     * <ul>
     * <li>If this command was defined by a Consumer, the Class of that consumer is returned.</li>
     * <li>If this command was defined by a Supplier, the Class of the result from that Supplier is returned.</li>
     * <li>If this command was defined by an Object, the Class of that Object is returned.</li>
     * <li>If this command was defined by a Class, then that Class is returned.</li>
     * <li>If this command is a sub-command defined by a Method, then the Class or Inner Class enclosing that Method is returned.</li>
     * </ul>
     *
     * @return a Class.
     */
    Class getDeclaringClass();

    /**
     * Returns the method that is used to invoke this command.
     * If this command was defined with a Consumer, this returns {@code null}.
     * @return a Method
     */
    Method getDeclaringMethod();

    /**
     * Creates a copy of the backing array and returns the parameters of this Command
     * @return an array of CommandParameters
     */
    CommandParameter[] getParameters();
}