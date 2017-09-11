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

import com.github.breadmoirai.bot.framework.command.impl.CommandHandle;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

@FunctionalInterface
public interface CommandPreprocessorFunction {

    /**
     * This method is ran before the command is executed. If you wish to continue the execution of the command, simply use the {@link java.lang.Runnable} passed and call {@link java.lang.Runnable#run() Runnable#run()} to trigger the next preprocessor or the activation of the command.
     * Calling the passed {@link java.lang.Runnable} again after the first invocation will have no effect.
     *
     * @param commandObj   The command object that will be used to call the commandHandle.
     * @param targetHandle The targetHandle. This is whatever the annotation was attached to.
     * @param event        the event that triggered things
     * @param next         call this method to keep the ball rolling. Failure to call this method will result in the command not being activated.
     */
    void process(Object commandObj, CommandHandle targetHandle, CommandEvent event);

}
