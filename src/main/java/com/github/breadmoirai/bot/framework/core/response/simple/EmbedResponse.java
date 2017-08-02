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

package com.github.breadmoirai.bot.framework.core.response.simple;

import com.github.breadmoirai.bot.framework.core.impl.Response;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.function.Consumer;

public class EmbedResponse extends Response {

    private MessageEmbed message;
    private Consumer<Message> consumer;

    public EmbedResponse(MessageEmbed message) {
        this.message = message;
    }

    @Override
    public Message buildMessage() {
        return new MessageBuilder().setEmbed(message).build();
    }

    public MessageEmbed getMessage() {
        return message;
    }

    @Override
    public void onSend(Message message) {
        if (consumer != null) consumer.accept(message);
    }

    public EmbedResponse andThen(Consumer<Message> onSend) {
        if (consumer == null) consumer = onSend;
        else consumer = consumer.andThen(onSend);
        return this;
    }

}
