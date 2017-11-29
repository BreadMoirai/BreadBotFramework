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
package com.github.breadmoirai.tests.client;

import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Type;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;

public class TypeTestKeyTest {

    @MainCommand("10plus")
    public String addTen(@Type(int.class) CommandArgument argument) {
        final int i = argument.parseInt() + 10;
        return String.valueOf(i);
    }

    @Command("10")
    public String addTenMore() {
        return "21";
    }
}
