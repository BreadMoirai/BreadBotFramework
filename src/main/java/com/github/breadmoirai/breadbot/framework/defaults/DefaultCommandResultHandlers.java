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
package com.github.breadmoirai.breadbot.framework.defaults;

import com.github.breadmoirai.breadbot.framework.internal.command.CommandResultManagerImpl;
import com.github.breadmoirai.breadbot.framework.response.menu.Menu;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class DefaultCommandResultHandlers {
    public void initialize(CommandResultManagerImpl manager) {
        manager.registerResultHandler(String.class,
                (command, event, result) -> event.reply(result));
        manager.registerResultHandler(MessageEmbed.class,
                (command, event, result) -> event.reply(result));
        manager.registerResultHandler(Message.class,
                (command, event, result) -> event.reply(result));
        manager.registerResultHandler(Emote.class,
                (command, event, result) -> event.replyReaction(result));
        manager.registerResultHandler(Menu.class,
                (command, event, result) -> event.replyWith());
    }
}
