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

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.response.CommandResponseManager;
import com.github.breadmoirai.breadbot.framework.response.RestActionExtension;
import com.github.breadmoirai.breadbot.framework.response.internal.CommandResponseMessage;
import com.github.breadmoirai.breadbot.framework.response.internal.CommandResponseReactionImpl;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;

public abstract class CommandEventInternal extends CommandEvent {

    private Command command;
    private CommandResponseManager manager;

    public CommandEventInternal(JDA api, long responseNumber, BreadBotClient client, boolean isHelpEvent) {
        super(api, responseNumber, client, isHelpEvent);
        manager = CommandResponseManager.factory.get();
    }

    @Override
    public Command getCommand() {
        return command;
    }

    public CommandResponseManager getManager() {
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
        final CommandResponseMessage resp = new CommandResponseMessage(getChannel());
        final CommandResponseMessage.RMessageBuilder builder = resp.builder();
        manager.accept(resp);
        return builder.append(message);
    }

    @Override
    public CommandResponseMessage.RMessageBuilder reply(Message message) {
        final CommandResponseMessage resp = new CommandResponseMessage(getChannel(), message);
        final CommandResponseMessage.RMessageBuilder builder = resp.builder();
        manager.accept(resp);
        return builder;
    }

    @Override
    public CommandResponseMessage.RMessageBuilder reply() {
        final CommandResponseMessage m = new CommandResponseMessage(getChannel());
        final CommandResponseMessage.RMessageBuilder builder = m.builder();
        manager.accept(m);
        return builder;
    }

    @Override
    public RestActionExtension<Void> replyReaction(Emote emote) {
        CommandResponseReactionImpl resp = new CommandResponseReactionImpl(() -> getMessage().addReaction(emote));
        manager.accept(resp);
        return resp;
    }

    @Override
    public RestActionExtension<Void> replyReaction(String emoji) {
        CommandResponseReactionImpl resp = new CommandResponseReactionImpl(() -> getMessage().addReaction(emoji));
        manager.accept(resp);
        return resp;
    }
}
