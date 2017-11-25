package test.commands;/*    Copyright 2017 Ton Ly
 
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

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Index;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Width;

public class NameCommand {

    @Command
    public void name(CommandEvent event, @Width(-1) String name) {
        event.reply(name);
    }

    @Command
    public void first(CommandEvent event, @Index(0) @Width(1) String name) {
        event.reply(name);
    }

    @Command
    public void last(CommandEvent event, @Index(1) @Width(-1) String name) {
        event.reply(name);
    }

}
