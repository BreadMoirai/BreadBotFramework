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
package com.github.breadmoirai.samurai7.core.impl;

import com.github.breadmoirai.samurai7.core.CommandEvent;
import com.github.breadmoirai.samurai7.core.ICommandEventFactory;
import com.github.breadmoirai.samurai7.core.SamuraiClient;
import com.github.breadmoirai.samurai7.modules.prefix.IPrefixModule;
import com.github.breadmoirai.samurai7.util.DiscordPatterns;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;

import java.util.regex.Matcher;

public class CommandEventFactoryImpl implements ICommandEventFactory {

    private final IPrefixModule prefixModule;

    public CommandEventFactoryImpl(CommandEngineBuilder client) {
        this.prefixModule = client.getModule(IPrefixModule.class);
    }


    @Override
    public CommandEvent createEvent(GenericGuildMessageEvent event, Message message, SamuraiClient client) {
        String prefix = prefixModule.getPrefix(event.getGuild().getIdLong());
        String contentRaw = message.getContentRaw();
        String key, content;
        final Matcher matcher = DiscordPatterns.USER_MENTION_PREFIX.matcher(contentRaw);
        if (matcher.matches()) {
            contentRaw = contentRaw.substring(matcher.end(1)).trim();
            final String[] split = DiscordPatterns.WHITE_SPACE.split(contentRaw, 2);
            key = split[0];
            content = split.length > 1 ? split[1].trim() : null;
            return new MessageReceivedCommandEvent(client ,event, message, prefix, key, content);
        } else {
            if (contentRaw.startsWith(prefix)) {
                final String[] split = DiscordPatterns.WHITE_SPACE.split(contentRaw.substring(prefix.length()), 2);
                key = split[0];
                content = split.length > 1 ? split[1].trim() : null;
                return new MessageReceivedCommandEvent(client, event, message, prefix, key, content);
            }
            return null;
        }
    }


}
