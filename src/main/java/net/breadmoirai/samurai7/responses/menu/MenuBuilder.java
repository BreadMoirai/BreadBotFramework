/*
 *      Copyright 2017 Ton Ly (BreadMoirai)
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
 *
 */
package net.breadmoirai.samurai7.responses.menu;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.message.DataMessage;

import java.util.function.Consumer;

public abstract class MenuBuilder {

    public ResponseMenu buildResponse(Consumer<EmbedBuilder> embedCustomizer) {
        final EmbedBuilder eb = new EmbedBuilder();
        embedCustomizer.accept(eb);
        return buildResponse(eb);
    }

    public ResponseMenu buildResponse(EmbedBuilder embed) {
        final Menu menu = build();
        menu.attachOptions(embed);
        menu.setMessage(new MessageBuilder().setEmbed(embed.build()).build());
        return new ResponseMenu(menu);
    }

    public ResponseMenu buildResponse(Message message) {
        final Menu menu = build();
        menu.setMessage(message);
        return new ResponseMenu(menu);
    }

    public ResponseMenu attachTo(Message message) {
        if (message instanceof DataMessage) {
            throw new UnsupportedOperationException("This menu can not be attached to Messages created from a MessageBuilder.");
        }
        final ResponseMenu responseMenu = new ResponseMenu(build());
        responseMenu.onSend(message);
        return responseMenu;
    }

    protected abstract Menu build();
}
