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

import com.github.breadmoirai.breadbot.framework.command.internal.ConfigureCommands;

import java.lang.annotation.*;


/**
 * This annotation should only be attached to static methods that have one parameter.
 * The intended Command's method that this method modifies should be in the same scope as this method.
 * This annotation is repeatable and can be used to configure multiple commands.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Repeatable(ConfigureCommands.class)
public @interface ConfigureCommand {

    /**
     * This indicates which command that the method this annotation is attached to should configure
     * @return a string that indicates the name of the command to configure
     */
    String value() default "";
}

