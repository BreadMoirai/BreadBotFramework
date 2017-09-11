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
package com.github.breadmoirai.bot.framework.command.builder;

import com.github.breadmoirai.bot.framework.command.impl.CommandHandle;

import java.util.function.Consumer;

public class CommandClassHandleBuilder implements CommandHandleBuilder {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String[] getKeys() {
        return null;
    }

    @Override
    public CommandHandle build() {
        return null;
    }

    public void configure(Consumer<CommandClassHandleBuilder> consumer) {
        consumer.accept(this);
    }
}
