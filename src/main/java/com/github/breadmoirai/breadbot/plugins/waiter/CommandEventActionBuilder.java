package com.github.breadmoirai.breadbot.plugins.waiter;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * This is an extension of EventActionBuilder.
 * Note: using {@link #condition(Predicate)}
 * will overwrite any conditions already set,
 * which is all of these extension methods.
 *
 * @param <T> the result type
 */
public interface CommandEventActionBuilder<T> extends EventActionBuilder<CommandEvent, T> {

    /**
     * defines possible prefixes for the command.
     * This will only catch CommandEvents that do
     * not correspond to a registered command.
     * This only supports a depth of 1 key as
     * unclaimed events will only have 1 key.
     *
     * @param keys var-arg of possible prefixes
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

    default CommandEventActionBuilder<T> from(Role... roles) {
        Checks.notEmpty(roles, "roles");
        Checks.noneNull(roles, "roles");
        long[] roleIds = new long[roles.length];
        for (int i = 0; i < roles.length; i++) {
            roleIds[i] = roles[i].getIdLong();
        }
        return fromRoles(roleIds);
    }

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

    default CommandEventActionBuilder<T> from(Member... members) {
        Checks.notEmpty(members, "members");
        Checks.noneNull(members, "members");
        long[] memberIds = new long[members.length];
        for (int i = 0; i < members.length; i++) {
            memberIds[i] = members[i].getUser().getIdLong();
        }
        return fromUsers(memberIds);
    }

    default CommandEventActionBuilder<T> from(User... users) {
        Checks.notEmpty(users, "users");
        Checks.noneNull(users, "users");
        long[] memberIds = new long[users.length];
        for (int i = 0; i < users.length; i++) {
            memberIds[i] = users[i].getIdLong();
        }
        return fromUsers(memberIds);
    }

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

    default CommandEventActionBuilder<T> in(Guild... guild) {
        Checks.notEmpty(guild, "guild");
        Checks.noneNull(guild, "guild");
        final long[] longs = new long[guild.length];
        for (int i = 0; i < guild.length; i++) {
            longs[i] = guild[i].getIdLong();
        }
        return inGuild(longs);
    }

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

    default CommandEventActionBuilder<T> in(Channel... channel) {
        Checks.notEmpty(channel, "channel");
        Checks.noneNull(channel, "channel");
        final long[] longs = new long[channel.length];
        for (int i = 0; i < channel.length; i++) {
            longs[i] = channel[i].getIdLong();
        }
        return inChannel(longs);
    }

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

    CommandEventActionBuilder<T> matching(Predicate<CommandEvent> condition);

}
