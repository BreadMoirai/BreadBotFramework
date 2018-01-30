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

package com.github.breadmoirai.breadbot.plugins.waiter;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.function.Predicate;

/**
 * This is an extension of EventActionBuilder.
 * Note: using {@link #condition(java.util.function.Predicate)}
 * will overwrite any conditions already set,
 * which is all of these extension methods.
 *
 * @param <T>
 *         the result type
 */
public interface ReactionEventActionBuilder<T> extends EventActionBuilderExtension<GenericMessageReactionEvent, T> {


    default ReactionEventActionBuilder<T> on(Message... messages) {
        Checks.notEmpty(messages, "messages");
        Checks.noneNull(messages, "messages");
        final long[] messageIds = new long[messages.length];
        for (int i = 0; i < messages.length; i++) {
            messageIds[i] = messages[i].getIdLong();
        }
        return onMessages(messageIds);
    }

    default ReactionEventActionBuilder<T> onMessages(long... messageIds) {
        Checks.notNull(messageIds, "messageIds");
        return matching(event -> {
            final long messageId = event.getMessageId();
            for (long id : messageIds) {
                if (id == messageId) {
                    return true;
                }
            }
            return false;
        });
    }


    ReactionEventActionBuilder<T> matching(Predicate<CommandEvent> condition);

    @Override
    default ReactionEventActionBuilder<T> from(Role... roles) {
        EventActionBuilderExtension.super.from(roles);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> fromRoles(long... roleIds) {
        EventActionBuilderExtension.super.fromRoles(roleIds);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> from(Member... members) {
        EventActionBuilderExtension.super.from(members);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> from(User... users) {
        EventActionBuilderExtension.super.from(users);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> fromUsers(long... userIds) {
        EventActionBuilderExtension.super.fromUsers(userIds);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> in(Guild... guild) {
        EventActionBuilderExtension.super.in(guild);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> inGuild(long... guildIds) {
        EventActionBuilderExtension.super.inGuild(guildIds);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> in(MessageChannel... channel) {
        EventActionBuilderExtension.super.in(channel);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> inChannel(long... channelIds) {
        EventActionBuilderExtension.super.inChannel(channelIds);
        return this;
    }


}
