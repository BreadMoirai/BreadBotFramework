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
package com.github.breadmoirai.breadbot.framework.response;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.response.simple.EditResponse;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.RestAction;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class CommandResponse implements Serializable {

    private transient BreadBotClient client;

    private long authorId, sourceMessageId, sourceChannelId, sourceGuildId;
    private long messageId, channelId, guildId;

    /**
     * This method is what is entry point for Responses in terms of sending a message.
     *
     * @param channel the target channel to send to.
     */
    public void sendTo(MessageChannel channel, BiConsumer<Message, CommandResponse> onSuccess, Consumer<Throwable> onFailure) {
        channel.sendMessage(buildMessage()).queue(m -> onSuccess.accept(m, this), onFailure);
    }

    /**
     * This method is called to get the message to send.
     *
     * @return the Message to send.
     */
    public abstract Message buildMessage();

    public void onFailure(Throwable t) {
        RestAction.DEFAULT_FAILURE.accept(t);
    }

    /**
     * This is the success consumer.
     *
     * @param message the sent message
     * @see CommandResponse#onFailure
     */
    public abstract void onSend(Message message);

    public final BreadBotClient getClient() {
        return client;
    }

    public final void setClient(BreadBotClient client) {
        this.client = client;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public long getSourceMessageId() {
        return sourceMessageId;
    }

    public void setSourceMessageId(long sourceMessageId) {
        this.sourceMessageId = sourceMessageId;
    }

    public long getSourceChannelId() {
        return sourceChannelId;
    }

    public void setSourceChannelId(long sourceChannelId) {
        this.sourceChannelId = sourceChannelId;
    }

    public long getSourceGuildId() {
        return sourceGuildId;
    }

    public void setSourceGuildId(long sourceGuildId) {
        this.sourceGuildId = sourceGuildId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    /**
     * Sets the fields of the passed CommandResponse to the fields of this Response unless already set. Then returns a new response that upon send, replaces this message with {@code newResponse}
     *
     * @param newResponse a CommandResponse
     */
    public void replaceWith(CommandResponse newResponse) {
        newResponse.setFieldsIfEmpty(this);
        EditResponse editResponse = new EditResponse(newResponse, getMessageId());
        editResponse.setFieldsIfEmpty(this);
        TextChannel channel = getClient().getJDA().getTextChannelById(getChannelId());
        if (channel != null)
            getClient().sendResponse(editResponse, channel);
    }

    public void setFieldsIfEmpty(CommandResponse response) {
        setFieldsIfEmpty(response.getAuthorId(), response.getSourceChannelId(), response.getSourceGuildId(), response.getMessageId(), response.getChannelId(), response.getGuildId(), response.getClient());
    }

    public void setFieldsIfEmpty(CommandEvent event) {
        setFieldsIfEmpty(event.getAuthorId(), event.getChannelId(), event.getGuildId(), 0, 0, 0, event.getClient());
    }

    public void setFieldsIfEmpty(long authorId, long sourceChannelId, long sourceGuildId, long messageId, long targetChannelId, long targetGuildId, BreadBotClient client) {
        if (authorId != 0 && getAuthorId() == 0) {
            setAuthorId(authorId);
        }
        if (sourceChannelId != 0 && getSourceChannelId() == 0) {
            setSourceChannelId(sourceChannelId);
        }
        if (sourceGuildId != 0 && getSourceGuildId() == 0) {
            setSourceGuildId(sourceGuildId);
        }
        if (messageId != 0 && getMessageId() == 0) {
            setMessageId(messageId);
        }
        if (targetChannelId != 0 && getChannelId() == 0) {
            setChannelId(targetChannelId);
        }
        if (targetGuildId != 0 && getGuildId() == 0) {
            setGuildId(targetGuildId);
        }
        if (client != null) {
            setClient(client);
        }
    }
}
