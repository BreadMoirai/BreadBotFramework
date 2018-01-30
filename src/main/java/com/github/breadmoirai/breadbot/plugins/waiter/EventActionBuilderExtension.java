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
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.utils.Checks;

import java.util.function.Predicate;

public interface EventActionBuilderExtension<E extends Event, V> extends EventActionBuilder<E, V> {

    default EventActionBuilderExtension<E, V> from(Role... roles) {
        Checks.notEmpty(roles, "roles");
        Checks.noneNull(roles, "roles");
        long[] roleIds = new long[roles.length];
        for (int i = 0; i < roles.length; i++) {
            roleIds[i] = roles[i].getIdLong();
        }
        return fromRoles(roleIds);
    }

    EventActionBuilderExtension<E, V> fromRoles(long... roleIds);

    default EventActionBuilderExtension<E, V> from(Member... members) {
        Checks.notEmpty(members, "members");
        Checks.noneNull(members, "members");
        long[] memberIds = new long[members.length];
        for (int i = 0; i < members.length; i++) {
            memberIds[i] = members[i].getUser().getIdLong();
        }
        return fromUsers(memberIds);
    }

    default EventActionBuilderExtension<E, V> from(User... users) {
        Checks.notEmpty(users, "users");
        Checks.noneNull(users, "users");
        long[] memberIds = new long[users.length];
        for (int i = 0; i < users.length; i++) {
            memberIds[i] = users[i].getIdLong();
        }
        return fromUsers(memberIds);
    }

    EventActionBuilderExtension<E, V> fromUsers(long... userIds);

    default EventActionBuilderExtension<E, V> in(Guild... guild) {
        Checks.notEmpty(guild, "guild");
        Checks.noneNull(guild, "guild");
        final long[] longs = new long[guild.length];
        for (int i = 0; i < guild.length; i++) {
            longs[i] = guild[i].getIdLong();
        }
        return inGuild(longs);
    }

    EventActionBuilderExtension<E, V> inGuild(long... guildIds);

    default EventActionBuilderExtension<E, V> in(MessageChannel... channel) {
        Checks.notEmpty(channel, "channel");
        Checks.noneNull(channel, "channel");
        final long[] longs = new long[channel.length];
        for (int i = 0; i < channel.length; i++) {
            longs[i] = channel[i].getIdLong();
        }
        return inChannel(longs);
    }

    EventActionBuilderExtension<E, V> inChannel(long... channelIds);

    EventActionBuilderExtension<E, V> matching(Predicate<E> condition);

}
