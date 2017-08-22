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

package com.github.breadmoirai.botframework.core.response.simple;

import com.github.breadmoirai.botframework.core.Response;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Consumer;

public abstract class BasicResponse extends Response {

    private Consumer<Message> onSuccess;
    private Consumer<Throwable> onFailure;

    @Override
    public void onSend(Message message) {
        if (onSuccess != null) {
            onSuccess.accept(message);
        }
    }

    @Override
    public void onFailure(Throwable t) {
        if (onFailure == null) {
            super.onFailure(t);
        } else onFailure.accept(t);
    }

    /**
     * Sets a consumer to be called when the message is sent
     */
    public BasicResponse uponSuccess(Consumer<Message> successConsumer) {
        this.onSuccess = successConsumer;
        return this;
    }

    /**
     *  Appends this consumer to the success consumer if it exists.
     *  Otherwise {@link com.github.breadmoirai.botframework.core.response.simple.BasicResponse#uponSuccess(Consumer)} is called
     */
    public BasicResponse withSuccess(Consumer<Message> successConsumer) {
        if (this.onSuccess == null) this.onSuccess = successConsumer;
        else this.onSuccess = this.onSuccess.andThen(successConsumer);
        return this;
    }

    /**
     * override the default failure consumer
     */
    public BasicResponse uponFailure(Consumer<Throwable> failureConsumer) {
        this.onFailure = failureConsumer;
        return this;
    }

    /**
     * appends failure behavior to any existing behavior
     * @see com.github.breadmoirai.botframework.core.Response#setDefaultFailure
     */
    @SuppressWarnings("Duplicates")
    public BasicResponse withFailure(Consumer<Throwable> failureConsumer) {
        if (onFailure == null) onFailure = t -> {
            super.onFailure(t);
            failureConsumer.accept(t);
        };
        else onFailure = onFailure.andThen(failureConsumer);
        return this;
    }
}
