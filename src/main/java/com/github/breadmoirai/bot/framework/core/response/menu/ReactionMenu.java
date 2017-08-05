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
package com.github.breadmoirai.bot.framework.core.response.menu;

import com.github.breadmoirai.bot.framework.core.response.menu.reactions.IMenuReaction;
import com.github.breadmoirai.bot.framework.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class ReactionMenu extends Menu {

    private List<IMenuReaction> reactions = new ArrayList<>();
    private BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> onReaction;

    ReactionMenu(List<IMenuReaction> reactions, BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> onReaction) {
        this.reactions = reactions;
        this.onReaction = onReaction;
    }

    @Override
    void attachOptions(EmbedBuilder embedBuilder) {
        final String collect = reactions.stream().filter(IMenuReaction::hasOption).map(IMenuReaction::getDisplay).collect(Collectors.joining("\n"));
        if (!collect.isEmpty())
            embedBuilder.addField("", collect, false);
    }

    @Override
    void waitForEvent(ResponseMenu responseMenu, EventWaiter waiter) {
        waiter.waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            if (event.getMessageIdLong() == responseMenu.getMessageId()) {
                final Optional<IMenuReaction> any = reactions.stream().filter(r -> r.matches(event)).findAny();
                if (any.isPresent()) {
                    final IMenuReaction r = any.get();
                    if (r.hasPredicate()) return r.apply(event, responseMenu);
                    else return onReaction != null && onReaction.test(event, responseMenu);
                }
            }
            return false;
        });
    }

    @Override
    void addReactions(Message message) {
        reactions.forEach(r -> r.addReactionTo(message).queue());
    }

    @Override
    void onDelete(ResponseMenu menu) {

    }


}
