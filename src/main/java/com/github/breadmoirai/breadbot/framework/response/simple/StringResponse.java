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

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.Iterator;
import java.util.Queue;
import java.util.function.Consumer;

public class StringResponse extends BasicResponse {

    private String message;
    private MessageBuilder.SplitPolicy[] policy;

    public StringResponse(String message) {
        this.message = message;
    }

    @Override
    public Message buildMessage() {
        return new MessageBuilder().append(message).buildAll(policy).peek();
    }

    @Override
    protected void send(MessageChannel channel) {
        final Queue<Message> messages = new MessageBuilder().append(message).buildAll(policy);
        Iterator<Message> iterator = messages.iterator();
        while (iterator.hasNext()) {
            Message m = iterator.next();
            if (iterator.hasNext())
                channel.sendMessage(m).queue();
            else
                channel.sendMessage(m).queue(this::onSend);
        }
    }

    public String getMessage() {
        return message;
    }

    public StringResponse setSplitPolicy(MessageBuilder.SplitPolicy... policy) {
        this.policy = policy;
        return this;
    }

    public StringResponse append(String s) {
        this.message += s;
        return this;
    }

    public StringResponse concat(StringResponse response) {
        return new StringResponse(this.message + '\n' + response.message);
    }

    @Override
    public StringResponse uponSuccess(Consumer<Message> successConsumer) {
        super.uponSuccess(successConsumer);
        return this;
    }

    @Override
    public StringResponse withSuccess(Consumer<Message> successConsumer) {
        super.withSuccess(successConsumer);
        return this;
    }

    @Override
    public StringResponse uponFailure(Consumer<Throwable> failureConsumer) {
        super.uponFailure(failureConsumer);
        return this;
    }

    @Override
    public StringResponse withFailure(Consumer<Throwable> failureConsumer) {
        super.withFailure(failureConsumer);
        return this;
    }
}
