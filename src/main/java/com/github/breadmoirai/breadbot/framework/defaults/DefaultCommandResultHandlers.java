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

package com.github.breadmoirai.breadbot.framework.defaults;

import com.github.breadmoirai.breadbot.framework.command.internal.CommandResultManagerImpl;
import com.github.breadmoirai.breadbot.util.Emoji;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class DefaultCommandResultHandlers {
    public void initialize(CommandResultManagerImpl manager) {
        manager.bindResultHandler(String.class,
                                  (command, event, result) -> event.reply(result).send());
        manager.bindResultHandler(MessageEmbed.class,
                                  (command, event, result) -> event.reply(new MessageBuilder().setEmbed(result).build())
                                          .send());
        manager.bindResultHandler(Message.class,
                                  (command, event, result) -> event.reply(result).send());
        manager.bindResultHandler(Emote.class,
                                  (command, event, result) -> event.replyReaction(result).send());
        manager.bindResultHandler(Emoji.class,
                                  (command, event, result) -> event.replyReaction(result.getUtf8()).send());
    }
}