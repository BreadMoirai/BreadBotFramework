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

import com.github.breadmoirai.framework.core.Response;
import com.github.breadmoirai.framework.core.response.menu.reactions.IMenuReaction;
import com.github.breadmoirai.framework.core.response.menu.reactions.MenuEmoji;
import com.github.breadmoirai.framework.core.response.menu.reactions.MenuEmote;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.function.Consumer;

public class ReactionResponse extends Response {

    private Consumer<Void> onSuccess;
    private Consumer<Throwable> onFailure;
    private final IMenuReaction reaction;

    public ReactionResponse(long messageId, String unicode) {
        this.setMessageId(messageId);
        this.reaction = new MenuEmoji(unicode, null, null);
    }

    public ReactionResponse(long messageId, Emote emote) {
        this.setMessageId(messageId);
        this.reaction = new MenuEmote(emote, null, null);
    }

    @Override
    public void onSend(Message message) {
        //hjirhh
    }

    @Override
    protected void send(MessageChannel channel) {
        reaction.addReactionTo(channel, getMessageId()).queue(this::onSend, this::onFailure);
    }

    private void onSend(Void nothing) {
        if (onSuccess != null) {
            onSuccess.accept(nothing);
        }
    }

    @Override
    public void onFailure(Throwable t) {
        if (onFailure == null) {
            super.onFailure(t);
        } else onFailure.accept(t);
    }

    public Consumer<Message> getAsConsumer() {
        return message -> reaction.addReactionTo(message.getChannel(), message.getIdLong()).queue(this::onSend, this::onFailure);
    }

    @Override
    public Message buildMessage() {
        return null;
    }

    @Override
    public EditResponse replace(long messageId) {
        throw new UnsupportedOperationException("You can't replace a reaction :thonk:");
    }

    public ReactionResponse uponSuccess(Consumer<Void> successConsumer) {
        this.onSuccess = successConsumer;
        return this;
    }

    public ReactionResponse withSuccess(Consumer<Void> successConsumer) {
        if (this.onSuccess == null) this.onSuccess = successConsumer;
        else this.onSuccess = this.onSuccess.andThen(successConsumer);
        return this;
    }

    public ReactionResponse uponFailure(Consumer<Throwable> failureConsumer) {
        this.onFailure = failureConsumer;
        return this;
    }

    @SuppressWarnings("Duplicates")
    public ReactionResponse withFailure(Consumer<Throwable> failureConsumer) {
        if (onFailure == null) onFailure = t -> {
            super.onFailure(t);
            failureConsumer.accept(t);
        };
        else onFailure = onFailure.andThen(failureConsumer);
        return this;
    }
}
