/*
 *      Copyright 2017 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package samurai7.core.engine;

import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Message;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;

public abstract class Response implements Serializable {

    private long authorId, messageId, channelId, guildId;
    private transient EventWaiter eventWaiter;
    private transient LongConsumer updateFunction;

    abstract public Message getMessage();

    abstract public void onSend(Message message);

    public final long getAuthorId() {
        return authorId;
    }

    public final void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public final long getMessageId() {
        return messageId;
    }

    public final void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public final long getChannelId() {
        return channelId;
    }

    public final void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public final long getGuildId() {
        return guildId;
    }

    public final void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    final void setEventWaiter(EventWaiter eventWaiter) {
        this.eventWaiter = eventWaiter;
    }

    protected final EventWaiter getEventWaiter() {return eventWaiter;}

    final void onSuccess(Message message) {
        setMessageId(message.getIdLong());
        this.onSend(message);
    }

    final void setUpdateFunction(LongConsumer updateFunction) {
        this.updateFunction = updateFunction;
    }
}
