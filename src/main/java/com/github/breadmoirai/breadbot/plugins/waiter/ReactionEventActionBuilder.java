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

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
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


    /**
     * Restrict condition to ReactionAddEvents only.
     * By default this will accept both reaction add and remove events.
     *
     * @return this
     */
    default ReactionEventActionBuilder<T> onAddOnly() {
        return matching(event -> event instanceof MessageReactionAddEvent);
    }

    /**
     * Restrict condition to ReactionRemoveEvents only.
     *
     * By default this will accept both reaction add and remove events.
     *
     * @return this
     */
    default ReactionEventActionBuilder<T> onRemoveOnly() {
        return matching(event -> event instanceof MessageReactionAddEvent);
    }

    /**
     * Matches the name of the reaction
     *
     * @return this
     */
    default ReactionEventActionBuilder<T> withName(String... emojiEmoteName) {
        Checks.notEmpty(emojiEmoteName, "emojiEmoteName");
        Checks.noneNull(emojiEmoteName, "emojiEmoteName");
        return matching(event -> {
            final String reactionEmote = event.getReaction().getReactionEmote().getName();
            for (String s : emojiEmoteName) {
                if (reactionEmote.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        });
    }

    default ReactionEventActionBuilder<T> withId(long... emoteIds) {
        Checks.notNull(emoteIds, "emoteIds");
        return matching(event -> {
            final MessageReaction.ReactionEmote reactionEmote = event.getReaction().getReactionEmote();
            final String id = reactionEmote.getId();
            if (id == null) return false;
            final long idLong = reactionEmote.getIdLong();
            for (long emoteId : emoteIds) {
                if (emoteId == idLong) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Uses the IntPredicate against the number of reactions after the
     * action is executed as a stop-if clause. If one is already set
     * with {@link #stopIf(ObjectIntPredicate)}, then this predicate
     * with be combined with the set one with an AND.
     *
     * @param reactionCount
     *         an IntPredicate that tests against the number of reactions
     *
     * @return this
     */
    ReactionEventActionBuilder<T> stopOnReactionCount(IntPredicate reactionCount);

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
            final long messageId = event.getMessageIdLong();
            for (long id : messageIds) {
                if (id == messageId) {
                    return true;
                }
            }
            return false;
        });
    }


    ReactionEventActionBuilder<T> matching(Predicate<GenericMessageReactionEvent> condition);

    @Override
    default ReactionEventActionBuilder<T> from(Role... roles) {
        EventActionBuilderExtension.super.from(roles);
        return this;
    }

    @SuppressWarnings("Duplicates")
    @Override
    default ReactionEventActionBuilder<T> fromRoles(long... roleIds) {
        Checks.notNull(roleIds, "roleIds");
        return matching((GenericMessageReactionEvent event) -> {
            final List<Role> roles = event.getMember().getRoles();
            for (Role r : roles) {
                final long id = r.getIdLong();
                for (long l : roleIds) {
                    if (id == l) return true;
                }
            }
            return false;
        });
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
        Checks.notNull(userIds, "userIds");
        if (userIds.length == 1) {
            final long userId = userIds[0];
            return matching(event -> event.getUser().getIdLong() == userId);
        }
        return matching(event -> {
            final long authorId = event.getUser().getIdLong();
            for (long userId : userIds) {
                if (authorId == userId) return true;
            }
            return false;
        });
    }

    @Override
    default ReactionEventActionBuilder<T> in(Guild... guild) {
        EventActionBuilderExtension.super.in(guild);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> inGuild(long... guildIds) {
        Checks.notNull(guildIds, "guildIds");
        return matching(event -> {
            final long guildId = event.getGuild().getIdLong();
            for (long aLong : guildIds) {
                if (aLong == guildId) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    default ReactionEventActionBuilder<T> in(MessageChannel... channel) {
        EventActionBuilderExtension.super.in(channel);
        return this;
    }

    @Override
    default ReactionEventActionBuilder<T> inChannel(long... channelIds) {
        Checks.notNull(channelIds, "channelIds");
        return matching(event -> {
            final long channelId = event.getChannel().getIdLong();
            for (long aLong : channelIds) {
                if (aLong == channelId) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    ReactionEventActionBuilder<T> condition(Predicate<GenericMessageReactionEvent> condition);

    @Override
    ReactionEventActionBuilder<T> action(Consumer<GenericMessageReactionEvent> action);

    @Override
    ReactionEventActionBuilder<T> stopIf(ObjectIntPredicate<GenericMessageReactionEvent> stopper);

    @Override
    <V2> ReactionEventActionBuilder<V2> finishWithResult(Function<GenericMessageReactionEvent, V2> finisher);

    @Override
    default ReactionEventActionBuilder<Void> finish(Runnable finisher) {
        return finishWithResult(event -> {
            finisher.run();
            return null;
        });
    }

    @Override
    ReactionEventActionBuilder<T> waitFor(long timeout, TimeUnit unit);

    @Override
    ReactionEventActionBuilder<T> timeout(Runnable timeoutAction);

    @Override
    EventActionFuture<T> build();
}
