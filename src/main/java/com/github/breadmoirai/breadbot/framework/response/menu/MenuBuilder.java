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

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Consumer;

public abstract class MenuBuilder {

    public MenuResponse buildResponse(Consumer<EmbedBuilder> embedCustomizer) {
        final EmbedBuilder eb = new EmbedBuilder();
        embedCustomizer.accept(eb);
        return buildResponse(eb);
    }

    public MenuResponse buildResponse(EmbedBuilder embed) {
        final Menu menu = build();
        menu.attachOptions(embed);
        return new MenuResponse(menu, new MessageBuilder().setEmbed(embed.build()).build(), false);
    }

    public MenuResponse buildResponse(Message message) {
        final Menu menu = build();
        return new MenuResponse(menu, message, false);
    }

    public MenuResponse attachTo(Message message) {
        if (message.getChannel() == null) {
            throw new UnsupportedOperationException("This menu can not be attached to Messages created from a MessageBuilder.");
        }
        return new MenuResponse(build(), message, true);
    }

    protected abstract Menu build();

}
