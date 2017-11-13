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

import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import com.github.breadmoirai.breadbot.framework.response.CommandResponseManager;
import com.github.breadmoirai.breadbot.framework.response.CommandResponsePacket;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultCommandResponseManager implements CommandResponseManager {

    @Override
    public void acceptResponse(CommandResponsePacket packet) {
        CommandResponse response = packet.getResponse();
        MessageChannel channel = packet.getTargetChannel();
        response.setChannelId(channel.getIdLong());
        if (channel.getType() == ChannelType.TEXT) {
            response.setGuildId(((TextChannel) channel).getGuild().getIdLong());
        }
        BiConsumer<Message, CommandResponse> onSuccess = (m, r) -> {
            r.setMessageId(m.getIdLong());
            r.onSend(m);
        };
        Consumer<Throwable> onFailure = response::onFailure;
        response.sendTo(channel, onSuccess, onFailure);
    }
}
