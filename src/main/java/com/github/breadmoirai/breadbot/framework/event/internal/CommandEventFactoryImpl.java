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

package com.github.breadmoirai.breadbot.framework.event.internal;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.event.CommandEventFactory;
import com.github.breadmoirai.breadbot.modules.prefix.PrefixModule;
import com.github.breadmoirai.breadbot.util.DiscordPatterns;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;

import java.util.regex.Matcher;

public class CommandEventFactoryImpl implements CommandEventFactory {

    private final PrefixModule prefixModule;
    private String myId;

    public CommandEventFactoryImpl(PrefixModule prefixSupplier) {
        this.prefixModule = prefixSupplier;
    }

    @Override
    public CommandEventInternal createEvent(GenericGuildMessageEvent event, Message message, BreadBotClient client) {
        String prefix = prefixModule.getPrefix(event.getGuild());
        String contentRaw = message.getContentRaw();
        final Matcher matcher = DiscordPatterns.USER_MENTION_PREFIX.matcher(contentRaw);
        if (matcher.find() && matcher.start() == 0 && matcher.group(1).equals(getMyId(event))) {
            contentRaw = contentRaw.substring(matcher.end()).trim();
            return parseContent(event, message, client, prefix, contentRaw);
        } else {
            if (contentRaw.startsWith(prefix)) {
                contentRaw = contentRaw.substring(prefix.length()).trim();
                return parseContent(event, message, client, prefix, contentRaw);
            }
            return null;
        }
    }

    private String getMyId(GenericGuildMessageEvent event) {
        if (myId == null) {
            myId = event.getJDA().getSelfUser().getId();
        }
        return myId;
    }

    private CommandEventInternal parseContent(GenericGuildMessageEvent event, Message message, BreadBotClient client, String prefix, String contentRaw) {
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
                return new MessageReceivedCommandEvent(client, event, message, prefix, new String[]{key2}, content2 != null ? content2 + " help" : "help", true);
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