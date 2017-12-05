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
package com.github.breadmoirai.breadbot.framework.internal.event;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.internal.response.CommandResponseEmojiImpl;
import com.github.breadmoirai.breadbot.framework.internal.response.CommandResponseMessage;
import com.github.breadmoirai.breadbot.framework.response.CommandResponseManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.requests.RestAction;

public abstract class CommandEventInternal extends CommandEvent {

    private CommandHandle command;
    private CommandResponseManager manager;

    public CommandEventInternal(JDA api, long responseNumber, BreadBotClient client, boolean isHelpEvent, CommandResponseManager manager) {
        super(api, responseNumber, client, isHelpEvent);
    }

    public void setCommand(CommandHandle command) {
        this.command = command;
    }

    @Override
    public CommandResponseMessage.Builder reply(Message message) {
        final CommandResponseMessage resp = new CommandResponseMessage(getChannel(), message);
        final CommandResponseMessage.Builder builder = resp.new Builder();
        manager.accept(resp);
        return builder;
    }

    @Override
    public CommandResponseEmojiImpl replyReaction(Emote emote) {

    }

    @Override
    public CommandResponseEmojiImpl replyReaction(String emoji) {
        final RestAction<Void> restAction = getChannel().addReactionById(getMessageId(), emoji);

    }
}
