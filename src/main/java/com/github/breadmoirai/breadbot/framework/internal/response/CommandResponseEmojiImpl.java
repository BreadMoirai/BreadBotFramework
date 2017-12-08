/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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
package com.github.breadmoirai.breadbot.framework.internal.response;

import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class CommandResponseEmojiImpl extends CommandResponse<Void> {

    private final Message message;
    private String emoji;
    private long delay;
    private TimeUnit unit;
    private Consumer<Void> success;
    private Consumer<Throwable> failure;

    public CommandResponseEmojiImpl(Message message) {
        this.message = message;
    }

    @Override
    public void dispatch(LongConsumer linkReceiver) {
        if (delay > 0) {
            message.addReaction(emoji).queueAfter(delay, unit, success, failure);
        } else {
            message.addReaction(emoji).queue(success, failure);
        }
    }

    @Override
    public void onMessageDelete(GuildMessageDeleteEvent event) {

    }

    @Override
    public void cancel() {

    }

}
