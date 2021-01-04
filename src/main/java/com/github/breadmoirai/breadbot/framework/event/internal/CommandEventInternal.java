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
package com.github.breadmoirai.breadbot.framework.event.internal;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.internal.BreadBotImpl;
import com.github.breadmoirai.breadbot.framework.response.ResponseManager;
import com.github.breadmoirai.breadbot.framework.response.RestActionExtension;
import com.github.breadmoirai.breadbot.framework.response.internal.CommandResponseMessage;
import com.github.breadmoirai.breadbot.framework.response.internal.CommandResponseReactionImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

public abstract class CommandEventInternal extends CommandEvent {

    private final BreadBotImpl client;
    private final ResponseManager manager;
    private Command command;

    public CommandEventInternal(JDA api, long responseNumber, BreadBotImpl client, boolean isHelpEvent) {
        super(api, responseNumber, client, isHelpEvent);
        this.client = client;
        manager = client.getResponseManager();
    }

    @Override
    public Command getCommand() {
        return command;
    }

    public ResponseManager getManager() {
        return manager;
    }

    public void setCommand(Command command) {
        this.command = command;
        String[] keys = command.getKeys();
        if (command.getParent() == null) return;
        for (String key : keys) {
            String contentL = getContent().toLowerCase();
            String keyL = key.toLowerCase();
            if (!contentL.contains(keyL)) return;
            int i = contentL.indexOf(keyL);
            String newContent = getContent().substring(i + keyL.length()).trim();
            super.argumentList = null;
            String contentK = getKeys()[0] + " " + getContent().substring(0, i + keyL.length()).trim();
            setKeys(contentK.split("\\s+"));
            setContent(newContent);
        }
    }

    protected abstract void setContent(String newContent);

    protected abstract void setKeys(String[] keys);

    @Override
    public CommandResponseMessage.RMessageBuilder reply(String message) {
        final CommandResponseMessage resp = new CommandResponseMessage(manager, getChannel());
        final CommandResponseMessage.RMessageBuilder builder = resp.builder();
        return builder.append(message);
    }

    @Override
    public CommandResponseMessage.RMessageBuilder reply(Message message) {
        final CommandResponseMessage resp = new CommandResponseMessage(manager, getChannel(), message);
        final CommandResponseMessage.RMessageBuilder builder = resp.builder();
        return builder;
    }

    @Override
    public CommandResponseMessage.RMessageBuilder reply() {
        final CommandResponseMessage m = new CommandResponseMessage(manager, getChannel());
        final CommandResponseMessage.RMessageBuilder builder = m.builder();
        return builder;
    }

    @Override
    public RestActionExtension<Void> replyReaction(Emote emote) {
        CommandResponseReactionImpl resp = new CommandResponseReactionImpl(manager,
                () -> getMessage().addReaction(emote));
        return resp;
    }

    @Override
    public RestActionExtension<Void> replyReaction(String emoji) {
        CommandResponseReactionImpl resp = new CommandResponseReactionImpl(manager,
                () -> getMessage().addReaction(emoji));
        return resp;
    }

    @Override
    public BreadBotImpl getClient() {
        return client;
    }
}
