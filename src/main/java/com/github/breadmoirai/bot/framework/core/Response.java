/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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
 */
package com.github.breadmoirai.bot.framework.core;

import com.github.breadmoirai.bot.framework.core.response.simple.EditResponse;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.Serializable;
import java.util.function.Consumer;

public abstract class Response implements Serializable {

    private transient SamuraiClient client;

    private long authorId, messageId, channelId, guildId;

    protected void send(MessageChannel channel) {
        channelId = channel.getIdLong();
        final Message message = buildMessage();
        if (message == null) return;

        final Consumer<Message> onSend = sent -> {
            this.setMessageId(sent.getIdLong());
            this.onSend(sent);
        };
        channel.sendMessage(message).queue(onSend, this::onFailure);
    }

    public void onFailure(Throwable t) {
        RestAction.DEFAULT_FAILURE.accept(t);
    }

    public final SamuraiClient getClient() {
        return client;
    }

    public final void setClient(SamuraiClient client) {
        this.client = client;
    }

    public abstract Message buildMessage();

    public abstract void onSend(Message message);

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

    public final void send() {
        client.send(this);
    }

    protected final void submitNewResponse(Response response) {
        base(response);
        client.send(response);
    }

    /**
     * This can only be used on messages sent by the bot.
     * If you want to edit a response in a different channel
     * first use {@link Response#setChannelId(long)   Response#setChannelId(long)}
     */
    public EditResponse replace(long messageId) {
        EditResponse editResponse = new EditResponse(this, messageId);
        editResponse.base(this);
        return editResponse;
    }

    /**
     * This can only be used on messages sent by the bot.
     * If you want to edit a response in a different channel
     * first use {@link Response#setChannelId(long)   Response#setChannelId(long)}
     */
    public EditResponse replaceWith(Response newResponse) {
        newResponse.base(this);
        EditResponse editResponse = new EditResponse(newResponse, getMessageId());
        editResponse.base(this);
        return editResponse;
    }


    /**
     * @see Response#base(long, long, long, long, SamuraiClient)
     */
    public void base(CommandEvent target) {
        base(target.getAuthorId(), target.getChannelId(), target.getGuildId(), target.getMessageId(), target.getClient());
    }

    /**
     * @see Response#base(long, long, long, long, SamuraiClient)
     */
    public void base(Response target) {
        base(target.getAuthorId(), target.getChannelId(), target.getGuildId(), target.getMessageId(), target.getClient());
    }

    /**
     * Sets the target of this response. I.E. where it's going. If any of the parameters passed are {@code 0} or {@code null} or the fields of this response are already set, it will not be overridden.
     * <p>Responses obtained from {@link com.github.breadmoirai.bot.framework.core.CommandEvent#respond CommandEvent#respond...} will already have their fields set.
     * @param authorId id of the author, depends on impl whether this is significant.
     * @param channelId id of the channel, a must have.
     * @param guildId id of the guild, optional.
     * @param messageId id of the message, required for reactionResponses, otherwise is automatically adjusted to the id of the message sent by this response.
     * @param client the client. particularity important.
     */
    public void base(long authorId, long channelId, long guildId, long messageId, SamuraiClient client) {
        if (getAuthorId() == 0 && authorId != 0) {
            setAuthorId(authorId);
        }
        if (getChannelId() == 0 && channelId != 0) {
            setChannelId(channelId);
        }
        if (getGuildId() == 0 && guildId != 0){
            setGuildId(guildId);
        }
        if (getMessageId() == 0 && guildId != 0) {
            setMessageId(messageId);
        }
        if (client != null) {
            setClient(client);
        }
    }
}
