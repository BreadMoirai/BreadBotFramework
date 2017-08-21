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

package com.github.breadmoirai.framework.core.response.simple;

import net.dv8tion.jda.core.entities.Message;

import java.util.function.Consumer;

public class MessageResponse extends BasicResponse {

    private Message message;

    public MessageResponse(Message message) {
        this.message = message;
    }

    @Override
    public Message buildMessage() {
        return message;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public MessageResponse uponSuccess(Consumer<Message> successConsumer) {
        super.uponSuccess(successConsumer);
        return this;
    }

    @Override
    public MessageResponse withSuccess(Consumer<Message> successConsumer) {
        super.withSuccess(successConsumer);
        return this;
    }

    @Override
    public MessageResponse uponFailure(Consumer<Throwable> failureConsumer) {
        super.uponFailure(failureConsumer);
        return this;
    }

    @Override
    public MessageResponse withFailure(Consumer<Throwable> failureConsumer) {
        super.withFailure(failureConsumer);
        return this;
    }
}
