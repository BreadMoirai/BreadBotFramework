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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

public class TestClient implements EventListener {

    private final int timeout = 25;
    private final long channelId;
    private JDA api;

    private long lastMessage;

    public TestClient(long channelId) {
        this.channelId = channelId;
    }

    @Override
    public void onEvent(Event event) {
        api = event.getJDA();
    }


    public void sendMessage(String message) {
        final TextChannel channel = api.getTextChannelById(channelId);
        final Message complete = channel.sendMessage(message).complete();
        lastMessage = complete.getIdLong();
    }

    public long getLastMessage() {
        return lastMessage;
    }
}
