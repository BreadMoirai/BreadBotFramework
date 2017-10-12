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
package com.github.breadmoirai.bot.framework.command.parameter;

import com.github.breadmoirai.bot.util.DateTimeMapper;
import com.github.breadmoirai.bot.util.DurationMapper;
import com.github.breadmoirai.bot.framework.event.Arguments;
import com.github.breadmoirai.bot.util.Emoji;
import net.dv8tion.jda.core.entities.*;

import java.time.Duration;
import java.time.OffsetDateTime;
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

    private static final Map<Class<?>, ArgumentParser<?>> map;

    static {
        map = new HashMap<>();


        final ArgumentTypePredicate intPredicate = (arg, flags) -> {
            final boolean hex = ArgumentFlags.hasFlag(flags, ArgumentFlags.HEX);
            return hex ? arg.isHex() : arg.isInteger();
        };
        final ArgumentTypeMapper<Integer> intType = (arg, flags) -> {
            final boolean hex = ArgumentFlags.hasFlag(flags, ArgumentFlags.HEX);
            if (hex) {
                try {
                    return Optional.of(Integer.parseInt(Arguments.stripHexPrefix(arg.getArgument()), 16));
                } catch (NumberFormatException ignored) {
                    return Optional.empty();
                }
            } else {
                return Optional.of(arg.parseInt());
            }
        };
        final ArgumentParser<Integer> intParser = new ArgumentParser<>(intPredicate, intType);
        map.put(INTEGER, intParser);
        map.put(Integer.class, intParser);


        final ArgumentParser<Long> longParser = new ArgumentParser<>((arg, flags) -> {
            final boolean hex = ArgumentFlags.hasFlag(flags, ArgumentFlags.HEX);
            return hex ? arg.isHex() : arg.isLong();
        }, (arg, flags) -> {
            final boolean hex = ArgumentFlags.hasFlag(flags, ArgumentFlags.HEX);
            if (hex) {
                try {
                    return Optional.of(Long.parseLong(Arguments.stripHexPrefix(arg.getArgument()), 16));
                } catch (NumberFormatException ignored) {

                }
            } else if (arg.isLong()) {
                return Optional.of(arg.parseLong());
            }
            return Optional.empty();
        });
        map.put(LONG, longParser);
        map.put(Long.class, longParser);

        registerArgumentMapperSimple(FLOAT, CommandArgument::isFloat, CommandArgument::parseFloat);
        registerArgumentMapperSimple(Float.class, CommandArgument::isFloat, CommandArgument::parseFloat);

        registerArgumentMapperSimple(DOUBLE, CommandArgument::isFloat, CommandArgument::parseDouble);
        registerArgumentMapperSimple(Double.class, CommandArgument::isFloat, CommandArgument::parseDouble);


        registerArgumentMapperSimple(BOOLEAN, CommandArgument::isBoolean, CommandArgument::parseBoolean);
        registerArgumentMapperSimple(Boolean.class, CommandArgument::isBoolean, CommandArgument::parseBoolean);

        registerArgumentMapper(RANGE, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                return arg.isRange();
            } else return arg.isInteger() || arg.isRange();
        }, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                return arg.isNumeric() ? Optional.empty() : Optional.of(arg.parseRange());
            } else return Optional.of(arg.parseRange());
        });

        registerArgumentMapper(USER, null, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                if (arg.isValidUser()) return Optional.of(arg.getUser());
                else return Optional.empty();
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                User user = arg.getEvent().getJDA().getUserById(l);
                return Optional.ofNullable(user);
            }
            return arg.findMember().map(Member::getUser);
        });

        registerArgumentMapper(MEMBER, null, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                if (arg.isValidMember()) return Optional.of(arg.getMember());
                else return Optional.empty();
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                Member member = arg.getEvent().getGuild().getMemberById(l);
                return Optional.ofNullable(member);
            }
            return arg.findMember();
        });

        registerArgumentMapper(ROLE, null, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                if (arg.isValidRole()) return Optional.of(arg.getRole());
                else return Optional.empty();
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                Role role = arg.getEvent().getGuild().getRoleById(l);
                if (role != null) {
                    return Optional.of(role);
                }
            }
            return arg.findRole();
        });

        registerArgumentMapper(TEXTCHANNEL, null, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                if (arg.isValidTextChannel()) return Optional.of(arg.getTextChannel());
                else return Optional.empty();
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                TextChannel channel = arg.getEvent().getJDA().getTextChannelById(l);
                if (channel != null) {
                    return Optional.of(channel);
                }
            }
            return arg.findTextChannel();
        });

        registerArgumentMapperSimple(EMOTE, CommandArgument::isEmote, CommandArgument::getEmote);

        registerArgumentMapperSimple(EMOJI, CommandArgument::isEmoji, CommandArgument::getEmoji);

        registerArgumentMapperSimple(Duration.class, null, new DurationMapper());

        registerArgumentMapperSimple(OffsetDateTime.class, null, new DateTimeMapper());

        registerArgumentMapperSimple(String.class, null, CommandArgument::getArgument);
    }

    /**
     * Registers an ArgumentMapper with the type provided.
     *
     * @param type   the Type class
     * @param mapper the mapper
     * @param <T>    the type
     */

    public static <T> void registerArgumentMapper(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        map.put(type, new ArgumentParser<>(predicate, mapper));
    }


    /**
     * This ignores flags. Use {@link com.github.breadmoirai.bot.framework.command.parameter.ArgumentTypes#registerArgumentMapper} otherwise.
     *
     * @param type      The type class
     * @param isType    predicate to test if the argument can be parsed to the type provided. This param can be left {@code null} if the complexity is close to {@code getAsType.apply(arg) != null}
     * @param getAsType A function to convert the argument to the type provided.
     * @param <T>       The type
     */
    public static <T> void registerArgumentMapperSimple(Class<T> type, Predicate<CommandArgument> isType, Function<CommandArgument, T> getAsType) {
        final ArgumentTypePredicate l = isType == null ? null : (arg, flags) -> isType.test(arg);
        final ArgumentTypeMapper<T> r = (arg, flags) -> Optional.ofNullable(getAsType.apply(arg));
        registerArgumentMapper(type, l, r);
    }

    /**
     * Attempts to map the CommandArgument to the type provided using the flags and a registered ArgumentMapper.
     * If an ArgumentMapper is not registered with the type requested, an empty Optional is returned.
     *
     * @param type  the intended type class
     * @param arg   the argument to map from
     * @param flags any {@link com.github.breadmoirai.bot.framework.command.parameter.ArgumentFlags flags}
     * @param <T>   the type
     * @return an Optional containing the result if successful. Otherwise empty.
     */
    public static <T> Optional<T> getAsType(Class<T> type, CommandArgument arg, int flags) {
        final ArgumentParser<T> parser = getParser(type);
        if (parser == null) return Optional.empty();
        return parser.parse(arg, flags);
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
     * Checks if the passed arg is
     * <pre><code>
     *     {@link com.github.breadmoirai.bot.framework.command.parameter.ArgumentTypes#getAsType(java.lang.Class, com.github.breadmoirai.bot.framework.command.parameter.CommandArgument, int) getAsType(type, arg, flags)}.isPresent();
     * </code></pre>
     */
    public static boolean isOfType(Class<?> type, CommandArgument arg, int flags) {
        final ArgumentParser<?> parser = getParser(type);
        if (parser == null) {
            return false;
        }
        return parser.test(arg, flags);
    }

    /**
     * Implemented as
     * <pre><code>
     *     {@link com.github.breadmoirai.bot.framework.command.parameter.ArgumentTypes#isOfType(java.lang.Class, com.github.breadmoirai.bot.framework.command.parameter.CommandArgument, int) isOfType(type, arg, 0)}
     * </code></pre>
     */
    public static boolean isOfType(Class<?> type, CommandArgument arg) {
        return isOfType(type, arg, 0);
    }


    /**
     * Returns the predicate mapper pair registered if found.
     *
     * @param type the class of the type as it was registered or one of the default types.
     * @param <T>  the type
     * @return an ArgumentParser if found. Else {@code null}.
     */
    public static <T> ArgumentParser<T> getParser(Class<T> type) {
        final ArgumentParser<?> pair = map.get(type);
        if (pair != null) {
            //noinspection unchecked
            return (ArgumentParser<T>) pair;
        } else return null;
    }


}
