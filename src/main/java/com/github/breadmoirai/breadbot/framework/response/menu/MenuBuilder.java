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

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Consumer;

public abstract class MenuBuilder {

    private long authorId;
    private long channelId;
    private long guildId;
    private long messageId;
    private BreadBotClient client;

    public ResponseMenu buildResponse(Consumer<EmbedBuilder> embedCustomizer) {
        final EmbedBuilder eb = new EmbedBuilder();
        embedCustomizer.accept(eb);
        return buildResponse(eb);
    }

    public ResponseMenu buildResponse(EmbedBuilder embed) {
        final Menu menu = build();
        menu.attachOptions(embed);
        menu.setMessage(new MessageBuilder().setEmbed(embed.build()).build());
        final ResponseMenu r = new ResponseMenu(menu);
        r.base(authorId, channelId, guildId, messageId, client);
        return r;
    }

    public ResponseMenu buildResponse(Message message) {
        final Menu menu = build();
        menu.setMessage(message);
        final ResponseMenu r = new ResponseMenu(menu);
        r.base(authorId, channelId, guildId, messageId, client);
        return r;
    }

    public ResponseMenu attachTo(Message message) {
        if (message.getChannel() == null) {
            throw new UnsupportedOperationException("This menu can not be attached to Messages created from a MessageBuilder.");
        }
        final ResponseMenu responseMenu = new ResponseMenu(build());
        responseMenu.onSend(message);
        return responseMenu;
    }

    protected abstract Menu build();

    public final void base(CommandEvent event) {
        final long authorId = event.getAuthorId();
        final long channelId = event.getChannelId();
        final long guildId = event.getGuildId();
        final long messageId = event.getMessageId();
        final BreadBotClient client = event.getClient();
        setAuthorId(authorId);
        setChannelId(channelId);
        setGuildId(guildId);
        setMessageId(messageId);
        setClient(client);
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public void setClient(BreadBotClient client) {
        this.client = client;
    }
}
