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
package com.github.breadmoirai.framework.core.response.menu.reactions;

import com.github.breadmoirai.framework.core.response.menu.ResponseMenu;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.function.BiPredicate;

public class MenuEmoji implements IMenuReaction {

    private final String e;
    private final String s;
    private final BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> a;

    public MenuEmoji(String e, String s, BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> a) {
        this.e = e;
        this.s = s;
        this.a = a;
    }


    @Override
    public boolean matches(GenericGuildMessageReactionEvent event) {
        return event.getReactionEmote().getEmote() == null && e.equals(event.getReaction().getEmote().getName());
    }

    @Override
    public boolean hasOption() {
        return s != null;
    }

    @Override
    public RestAction<Void> addReactionTo(Message message) {
        return message.addReaction(e);
    }

    @Override
    public RestAction<Void> addReactionTo(MessageChannel channel, long messageId) {
       return channel.addReactionById(messageId, e);
    }

    @Override
    public boolean hasPredicate() {
        return a != null;
    }

    @Override
    public boolean apply(GenericGuildMessageReactionEvent event, ResponseMenu menu) {
        return a.test(event, menu);
    }

    @Override
    public String getDisplay() {
        return e + " " + s;
    }
}
