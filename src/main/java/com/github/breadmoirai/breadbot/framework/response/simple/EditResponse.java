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
package com.github.breadmoirai.breadbot.framework.response.simple;

import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EditResponse extends CommandResponse {
    private final CommandResponse response;

    public EditResponse(CommandResponse response, long messageId) {
        this.response = response;

        this.setAuthorId(response.getAuthorId());
        this.setGuildId(response.getGuildId());
        this.setChannelId(response.getChannelId());
        this.setMessageId(messageId);
    }

    @Override
    public void sendTo(MessageChannel channel, BiConsumer<Message, CommandResponse> onSuccess, Consumer<Throwable> onFailure) {
        final Message message = buildMessage();
        if (message == null) return;
        channel.editMessageById(getMessageId(), message).queue(m -> onSuccess.accept(m, response), this::onFailure);
    }


    @Override
    public Message buildMessage() {
        return response.buildMessage();
    }

    @Override
    public void onSend(Message message) {
        response.onSend(message);
    }

}
