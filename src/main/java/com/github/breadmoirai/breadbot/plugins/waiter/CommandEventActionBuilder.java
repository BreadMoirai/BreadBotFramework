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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This is an extension of EventActionBuilder.
 *
 * @param <T>
 *         the result type
 */
public interface CommandEventActionBuilder<T> extends EventActionBuilderExtension<CommandEvent, T> {

    /**
     * defines possible prefixes for the command.
     * This will only catch CommandEvents that do
     * not correspond to a registered command.
     * This only supports a depth of 1 key as
     * unclaimed events will only have 1 key.
     *
     * @param keys
     *         var-arg of possible prefixes
     *
     * @return this
     */
    default CommandEventActionBuilder<T> withKeys(String... keys) {
        Checks.noneBlank(Arrays.asList(keys), "keys");
        Checks.notEmpty(keys, "keys");
        return matching(event -> {
            final String key = event.getKey();
            for (String prefix : keys) {
                if (prefix.equalsIgnoreCase(key)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    default CommandEventActionBuilder<T> from(Role... roles) {
        EventActionBuilderExtension.super.from(roles);
        return this;
    }

    @SuppressWarnings("Duplicates")
    @Override
    default CommandEventActionBuilder<T> fromRoles(long... roleIds) {
        Checks.notNull(roleIds, "roleIds");
        return matching(event -> {
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
    default CommandEventActionBuilder<T> from(Member... members) {
        EventActionBuilderExtension.super.from(members);
        return this;
    }

    @Override
    default CommandEventActionBuilder<T> from(User... users) {
        EventActionBuilderExtension.super.from(users);
        return this;
    }

    @Override
    default CommandEventActionBuilder<T> fromUsers(long... userIds) {
        Checks.notNull(userIds, "userIds");
        if (userIds.length == 1) {
            final long userId = userIds[0];
            return matching(event -> event.getAuthorId() == userId);
        }
        return matching(event -> {
            final long authorId = event.getAuthorId();
            for (long userId : userIds) {
                if (authorId == userId) return true;
            }
            return false;
        });
    }

    @Override
    default CommandEventActionBuilder<T> in(Guild... guild) {
        EventActionBuilderExtension.super.in(guild);
        return this;
    }

    @Override
    default CommandEventActionBuilder<T> inGuild(long... guildIds) {
        Checks.notNull(guildIds, "guildIds");
        return matching(event -> {
            final long guildId = event.getGuildId();
            for (long aLong : guildIds) {
                if (aLong == guildId) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    default CommandEventActionBuilder<T> in(MessageChannel... channel) {
        EventActionBuilderExtension.super.in(channel);
        return this;
    }

    @Override
    default CommandEventActionBuilder<T> inChannel(long... channelIds) {
        Checks.notNull(channelIds, "channelIds");
        return matching(event -> {
            final long channelId = event.getChannelId();
            for (long aLong : channelIds) {
                if (aLong == channelId) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    CommandEventActionBuilder<T> matching(Predicate<CommandEvent> condition);

    @Override
    CommandEventActionBuilder<T> condition(Predicate<CommandEvent> condition);

    @Override
    CommandEventActionBuilder<T> action(Consumer<CommandEvent> action);

    @Override
    CommandEventActionBuilder<T> stopIf(ObjectIntPredicate<CommandEvent> stopper);

    @Override
    <V2> CommandEventActionBuilder<V2> finishWithResult(Function<CommandEvent, V2> finisher);

    @Override
    CommandEventActionBuilder<T> waitFor(long timeout, TimeUnit unit);

    @Override
    CommandEventActionBuilder<T> timeout(Runnable timeoutAction);

    @Override
    EventActionFuture<T> build();
}
