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
package com.github.breadmoirai.breadbot.framework.internal;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import com.github.breadmoirai.breadbot.framework.response.CommandResponsePacket;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CommandResponsePacketImpl implements CommandResponsePacket {

    private final CommandEvent event;
    private final CommandResponse response;
    private final MessageChannel targetChannel;

    public CommandResponsePacketImpl(CommandEvent event, CommandResponse response, MessageChannel targetChannel) {
        this.event = event;
        this.response = response;
        this.targetChannel = targetChannel;
        if (event != null)
            response.setFieldsIfEmpty(event);
    }

    @Override
    public CommandEvent getSourceEvent() {
        return event;
    }

    @Override
    public MessageChannel getTargetChannel() {
        return targetChannel;
    }

    @Override
    public CommandResponse getResponse() {
        return response;
    }

}
