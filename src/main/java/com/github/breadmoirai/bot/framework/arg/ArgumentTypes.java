/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.bot.framework.arg;

import com.github.breadmoirai.bot.framework.arg.impl.ArgumentTypeSimpleImpl;
import com.github.breadmoirai.bot.framework.event.Arguments;
import com.github.breadmoirai.bot.util.Emoji;
import net.dv8tion.jda.core.entities.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Does argument Mapping.
 * Is basically a heterogeneous map of Class<?> to ArgumentMapper<?>
 */
public final class ArgumentTypes {
    public static final Class<Integer> INTEGER = Integer.TYPE;
    public static final Class<Long> LONG = Long.TYPE;
    public static final Class<Float> FLOAT = Float.TYPE;
    public static final Class<Double> DOUBLE = Double.TYPE;
    public static final Class<Boolean> BOOLEAN = Boolean.TYPE;
    public static final Class<IntStream> RANGE = IntStream.class;
    public static final Class<User> USER = User.class;
    public static final Class<Member> MEMBER = Member.class;
    public static final Class<Role> ROLE = Role.class;
    public static final Class<TextChannel> TEXTCHANNEL = TextChannel.class;
    public static final Class<Emote> EMOTE = Emote.class;
    public static final Class<Emoji> EMOJI = Emoji.class;

    private static final Map<Class<?>, ArgumentMapper<?>> map;

    static {
        map = new HashMap<>();

        final ArgumentMapper<Integer> intType = (arg, flags) -> {
            final boolean hex = ArgumentFlags.hasFlag(ArgumentFlags.HEX, flags);
            if (hex && arg.isHex()) {
                try {
                    return Optional.of(Integer.parseInt(Arguments.stripHexPrefix(arg.getArgument()), 16));
                } catch (NumberFormatException ignored) {

                }
            } else if (arg.isInteger()) {
                return Optional.of(arg.parseInt());
            }
            return Optional.empty();
        };
        map.put(INTEGER, intType);
        map.put(Integer.class, intType);

        final ArgumentMapper<Long> longType = (arg, flags) -> {
            final boolean hex = ArgumentFlags.hasFlag(ArgumentFlags.HEX, flags);
            if (hex && arg.isHex()) {
                try {
                    return Optional.of(Long.parseLong(Arguments.stripHexPrefix(arg.getArgument()), 16));
                } catch (NumberFormatException ignored) {

                }
            } else if (arg.isLong()) {
                return Optional.of(arg.parseLong());
            }
            return Optional.empty();
        };
        map.put(LONG, longType);
        map.put(Long.class, longType);

        final ArgumentMapper<Float> floatType = new ArgumentTypeSimpleImpl<>(CommandArgument::isFloat, CommandArgument::parseFloat);
        map.put(FLOAT, floatType);
        map.put(Float.class, floatType);

        final ArgumentMapper<Double> doubleType = new ArgumentTypeSimpleImpl<>(CommandArgument::isFloat, CommandArgument::parseDouble);
        map.put(DOUBLE, doubleType);
        map.put(Double.class, doubleType);

        final ArgumentMapper<Boolean> boolType = new ArgumentTypeSimpleImpl<>(commandArgument -> {
            final String s = commandArgument.getArgument();
            return s.equals("true") || s.equals("false");
        }, commandArgument -> {
            final String s = commandArgument.getArgument();
            return s.equals("true");
        });
        map.put(BOOLEAN, boolType);
        map.put(Boolean.class, boolType);

        registerArgumentMapper(RANGE, (arg, flags) -> {
            if ((ArgumentFlags.isStrict(flags) && arg.isInteger())
                    || (arg.isRange() || arg.isInteger())) {
                return Optional.of(arg.parseRange());
            }
            return Optional.empty();
        });

        registerArgumentMapper(USER, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                return arg.isValidUser() ? Optional.of(arg.getUser()) : Optional.empty();
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                User user = arg.getJDA().getUserById(l);
                if (user != null) {
                    return Optional.of(user);
                }
            }
            return arg.findMember().map(Member::getUser);
        });

        registerArgumentMapper(MEMBER, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                return arg.isValidMember() ? Optional.of(arg.getMember()) : Optional.empty();
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                Member member = arg.getGuild().getMemberById(l);
                if (member != null) {
                    return Optional.of(member);
                }
            }
            return arg.findMember();
        });

        registerArgumentMapper(ROLE, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                return arg.isValidRole() ? Optional.of(arg.getRole()) : Optional.empty();
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                Role role = arg.getGuild().getRoleById(l);
                if (role != null) {
                    return Optional.of(role);
                }
            }
            return arg.findRole();
        });

        registerArgumentMapper(TEXTCHANNEL, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                return arg.isValidTextChannel() ? Optional.of(arg.getTextChannel()) : Optional.empty();
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                TextChannel channel = arg.getJDA().getTextChannelById(l);
                if (channel != null) {
                    return Optional.of(channel);
                }
            }
            return arg.findTextChannel();
        });

        registerArgumentMapper(EMOTE, (arg, flags) -> arg.isEmote() ? Optional.of(arg.getEmote()) : Optional.empty());

        registerArgumentMapper(EMOJI, (arg, flags) -> arg.isEmoji() ? Optional.of(arg.getEmoji()) : Optional.empty());
    }

    /**
     * Registers an ArgumentMapper with the type provided.
     *
     * @param type   the Type class
     * @param mapper the mapper
     * @param <T>    the type
     */
    public static <T> void registerArgumentMapper(Class<T> type, ArgumentMapper<T> mapper) {
        map.put(type, mapper);
    }


    /**
     * This ignores flags. Use {@link com.github.breadmoirai.bot.framework.arg.ArgumentTypes#registerArgumentMapper(java.lang.Class, com.github.breadmoirai.bot.framework.arg.ArgumentMapper)} otherwise.
     *
     * @param type      The type class
     * @param isType    predicate to test if the argument can be parsed to the type provided.
     * @param getAsType A function to actually convert the argument to the type provided.
     * @param <T>       The type
     */
    public static <T> void registerArgumentMapper(Class<T> type, Predicate<CommandArgument> isType, Function<CommandArgument, T> getAsType) {
        registerArgumentMapper(type, new ArgumentTypeSimpleImpl<>(isType, getAsType));
    }

    /**
     * Attempts to map the CommandArgument to the type provided using the flags and a registered ArgumentMapper.
     * If an ArgumentMapper is not registered with the type requested, an empty Optional is returned.
     *
     * @param type  the intended type class
     * @param arg   the argument to map from
     * @param flags any {@link com.github.breadmoirai.bot.framework.arg.ArgumentFlags flags}
     * @param <T>   the type
     * @return an Optional containing the result if successful. Otherwise empty.
     */
    public static <T> Optional<T> getAsType(Class<T> type, CommandArgument arg, int flags) {
        return getMapper(type).map(arg, flags);
    }

    /**
     * Attempts to map the CommandArgument to the type provided using the flags and a registered ArgumentMapper.
     * If an ArgumentMapper is not registered with the type requested, an empty Optional is returned.
     *
     * @param type the intended type class
     * @param arg  the argument to map from
     * @param <T>  the type
     * @return an Optional containing the result if successful. Otherwise empty.
     */
    public static <T> Optional<T> getAsType(Class<T> type, CommandArgument arg) {
        return getAsType(type, arg, 0);
    }

    /**
     * Implemented as
     * <pre><code>
     *     {@link com.github.breadmoirai.bot.framework.arg.ArgumentTypes#getAsType(java.lang.Class, com.github.breadmoirai.bot.framework.arg.CommandArgument, int) getAsType(type, arg, flags)}.isPresent();
     * </code></pre>
     */
    public static boolean isOfType(Class<?> type, CommandArgument arg, int flags) {
        return getAsType(type, arg, flags).isPresent();
    }

    /**
     * Implemented as
     * <pre><code>
     *     {@link com.github.breadmoirai.bot.framework.arg.ArgumentTypes#getAsType(java.lang.Class, com.github.breadmoirai.bot.framework.arg.CommandArgument) getAsType(type, arg, flags)}.isPresent();
     * </code></pre>
     */
    public static boolean isOfType(Class<?> type, CommandArgument arg) {
        return isOfType(type, arg, 0);
    }


    public static <T> ArgumentMapper<T> getMapper(Class<T> type) {
        //noinspection unchecked
        return (ArgumentMapper<T>) map.getOrDefault(type, ArgumentMapper.getEmptyMapper(type));
    }
}
