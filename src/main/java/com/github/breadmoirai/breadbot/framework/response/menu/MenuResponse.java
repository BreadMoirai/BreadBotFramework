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
package com.github.breadmoirai.breadbot.framework.response.menu;

import com.github.breadmoirai.breadbot.framework.response.CloseableResponse;
import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import com.github.breadmoirai.breadbot.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MenuResponse extends CommandResponse implements CloseableResponse {

    private transient final Menu menu;
    private transient Message message;
    private final boolean attach;

    protected MenuResponse(Menu menu, Message build, boolean attach) {
        this.menu = menu;
        this.message = build;
        this.attach = attach;
    }

    @Override
    public void sendTo(MessageChannel channel, BiConsumer<Message, CommandResponse> onSuccess, Consumer<Throwable> onFailure) {
        if (!attach)
            super.sendTo(channel, onSuccess, onFailure);
        else {
            if (message.getChannel() == null) {
                throw new UnsupportedOperationException("Cannot attach a menu to a message created from a MessageBuilder");
            }
            onSuccess.accept(message, this);
        }
    }

    @Override
    public Message buildMessage() {
        return message;
    }

    @Override
    public final void onSend(Message message) {
        this.message = message;
        menu.addReactions(message);
        menu.waitForEvent(this, EventWaiter.get());
    }

    /**
     * deletes the menu.
     */
    public void delete() {
        message.delete().queue();
    }

    @Override
    public void cancel(CommandResponse cancelMessage, boolean clearReactions) {
        if (clearReactions)
            message.clearReactions().queue();
        replaceWith(cancelMessage);
    }

}