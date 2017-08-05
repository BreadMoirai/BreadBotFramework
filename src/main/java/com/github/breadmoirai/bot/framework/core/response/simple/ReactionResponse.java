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

import com.github.breadmoirai.bot.framework.core.Response;
import com.github.breadmoirai.bot.framework.core.response.menu.reactions.IMenuReaction;
import com.github.breadmoirai.bot.framework.core.response.menu.reactions.MenuEmoji;
import com.github.breadmoirai.bot.framework.core.response.menu.reactions.MenuEmote;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.function.Consumer;

public class ReactionResponse extends Response {
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
    protected void send(MessageChannel channel) {
        reaction.addReactionTo(channel, getMessageId()).queue(null, );
    }

    public Consumer<Message> getAsConsumer() {
        return message -> reaction.addReactionTo(message.getChannel(), getMessageId());
    }

    @Override
    public Message buildMessage() {
        return null;
    }

    @Override
    public EditResponse replace(long messageId) {
        throw new UnsupportedOperationException("You can't replace a reaction");
    }
    
    public EditResponse onSuccess(Consumer<Void> successConsumer) {
        super.onSuccess(successConsumer);
        return this;
    }

    public EditResponse andThen(Consumer<Void> successConsumer) {
        super.andThen(successConsumer);
        return this;
    }


    public EditResponse onFailure(Consumer<Throwable> failureConsumer) {
        super.onFailure(failureConsumer);
        return this;
    }


    public EditResponse withFailure(Consumer<Throwable> failureConsumer) {
        super.withFailure(failureConsumer);
        return this;
    }
}
