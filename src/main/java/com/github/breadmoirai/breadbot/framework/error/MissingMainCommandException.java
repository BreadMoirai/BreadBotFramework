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
package com.github.breadmoirai.breadbot.framework.error;

public class MissingMainCommandException extends CommandInitializationException {

    public MissingMainCommandException(Class<?> commandClass) {
        super("If you want to register " + commandClass.getSimpleName() + " as a single command, you must add an @MainCommand annotation to one method." +
                " Otherwise use #createCommands or #addCommands instead to register as multiple commands.");
    }
}