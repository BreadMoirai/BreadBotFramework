/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.event;

import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.breadbot.framework.event.internal.MessageReceivedCommandEvent;
import com.github.breadmoirai.breadbot.framework.internal.BreadBotImpl;
import com.github.breadmoirai.breadbot.plugins.prefix.PrefixPlugin;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;

import java.util.function.Predicate;

public class CommandEventFactory {

    private final PrefixPlugin prefixModule;
    private String myId;
    private Predicate<Message> preProcessPredicate;
    private String mention;
    private String nmention;

    public CommandEventFactory(PrefixPlugin prefixSupplier) {
        this.prefixModule = prefixSupplier;
    }

    public CommandEventInternal createEvent(GenericGuildMessageEvent event, Message message, BreadBotImpl client) {
        String prefix = prefixModule.getPrefix(event.getGuild());
        String contentRaw = message.getContentRaw();

        if (contentRaw.startsWith(prefix)) {
            if (checkMessage(message)) {
                final String trim = contentRaw.substring(prefix.length()).trim();
                return parseContent(event, message, client, prefix, trim);
            }
        } else {
            final String mention = getMention(false, event);
            if (contentRaw.startsWith(mention)) {
                if (checkMessage(message)) {
                    final String s = contentRaw.substring(mention.length()).trim();
                    return parseContent(event, message, client, prefix, s);
                }
            } else {
                final String mention1 = getMention(true, event);
                if (contentRaw.startsWith(mention1)) {
                    if (checkMessage(message)) {
                        final String s = contentRaw.substring(mention1.length()).trim();
                        return parseContent(event, message, client, prefix, s);
                    }
                }
            }
        }
        return null;
    }

    public void setPreprocessor(Predicate<Message> preProcessPredicate) {
        this.preProcessPredicate = preProcessPredicate;
    }

    private boolean checkMessage(Message m) {
        return preProcessPredicate == null || preProcessPredicate.test(m);
    }

    private String getMyId(GenericGuildMessageEvent event) {
        if (myId == null) {
            myId = event.getJDA().getSelfUser().getId();
        }
        return myId;
    }

    private String getMention(boolean nick, GenericGuildMessageEvent event) {
        if (!nick) {
            if (mention == null) {
                mention = String.format("<@%s>", getMyId(event));
            }
            return mention;
        } else {
            if (nmention == null) {
                nmention = String.format("<@!%s>", getMyId(event));
            }
            return nmention;
        }
    }

    private CommandEventInternal parseContent(GenericGuildMessageEvent event, Message message, BreadBotImpl client,
                                              String prefix, String contentRaw) {
        final String[] split = splitContent(contentRaw);
        final String key = split[0];
        final String content = split[1];
        if (key.equalsIgnoreCase("help")) {
            if (content == null)
                return new MessageReceivedCommandEvent(client, event, message, prefix, new String[]{key}, null, true);
            else {
                final String[] split2 = splitContent(content);
                final String key2 = split2[0];
                final String content2 = split2[1];
                return new MessageReceivedCommandEvent(client, event, message, prefix, new String[]{key2},
                                                       content2 != null ? content2 + " help" : "help", true);
            }
        }
        return new MessageReceivedCommandEvent(client, event, message, prefix, new String[]{key}, content, false);
    }

    private String[] splitContent(String contentRaw) {
        if (contentRaw == null) {
            return new String[]{null, null};
        }
        int i = 0;
        int j = -1;
        for (; i < contentRaw.length(); i++) {
            if (Character.isWhitespace(contentRaw.charAt(i))) {
                j = i + 1;
                while (j < contentRaw.length()) {
                    if (Character.isWhitespace(contentRaw.charAt(j))) {
                        j++;
                    } else {
                        break;
                    }
                }
                break;
            }
        }

        if (i == contentRaw.length()) {
            return new String[]{contentRaw, null};
        } else {
            return new String[]{contentRaw.substring(0, i), contentRaw.substring(j)};
        }
    }

}