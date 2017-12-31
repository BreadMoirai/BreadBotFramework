/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.defaults;

import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParserFlags;
import com.github.breadmoirai.breadbot.framework.parameter.internal.CommandParameterTypeManagerImpl;
import com.github.breadmoirai.breadbot.util.Arguments;
import com.github.breadmoirai.breadbot.util.DateTimeMapper;
import com.github.breadmoirai.breadbot.util.DurationMapper;
import com.github.breadmoirai.breadbot.util.Emoji;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.stream.IntStream;

public class DefaultCommandParameters {

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

    public void initialize(CommandParameterTypeManagerImpl map) {
        final TypeParser<Integer> intParser = (arg, flags) -> {
            final boolean hex = TypeParserFlags.has(flags, TypeParserFlags.HEX);
            if (hex) {
                if (arg.isHex()) {
                    return arg.parseIntFromHex();
                } else return null;
            } else if (arg.isInteger()) {
                return arg.parseInt();
            } else {
                return null;
            }
        };
        map.put(INTEGER, intParser);
        map.put(Integer.class, intParser);


        final TypeParser<Long> longParser = (arg, flags) -> {
            final boolean hex = TypeParserFlags.has(flags, TypeParserFlags.HEX);
            if (hex && arg.isHex()) {
                return Long.parseLong(Arguments.stripHexPrefix(arg.getArgument()), 16);
            } else if (arg.isLong()) {
                return arg.parseLong();
            } else {
                return null;
            }
        };
        map.put(LONG, longParser);
        map.put(Long.class, longParser);


        final TypeParser<Float> floatParser = (arg, flags) -> arg.isFloat() ? arg.parseFloat() : null;
        map.put(FLOAT, floatParser);
        map.put(Float.class, floatParser);


        final TypeParser<Double> doubleParser = (arg, flags) -> arg.isFloat() ? arg.parseDouble() : null;
        map.put(DOUBLE, floatParser);
        map.put(Double.class, floatParser);


        final TypeParser<Boolean> boolParser = (arg, flags) -> arg.isBoolean() ? arg.parseBoolean() : null;
        map.put(BOOLEAN, floatParser);
        map.put(Boolean.class, floatParser);

        map.put(RANGE, (arg, flags) -> arg.parseRange());

        map.put(USER, (arg, flags) -> {
            if (TypeParserFlags.has(flags, "strict")) {
                if (arg.isValidUser()) return arg.getUser();
                else return null;
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                return arg.getEvent().getJDA().getUserById(l);
            }
            return arg.findMember().map(Member::getUser).orElse(null);
        });

        map.put(MEMBER, (arg, flags) -> {
            if (TypeParserFlags.has(flags, "strict")) {
                if (arg.isValidMember()) return arg.getMember();
                else return null;
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                return arg.getEvent().getGuild().getMemberById(l);
            }
            return arg.findMember().orElse(null);
        });

        map.put(ROLE, (arg, flags) -> {
            if (TypeParserFlags.has(flags, "strict")) {
                if (arg.isValidRole()) return arg.getRole();
                else return null;
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                Role role = arg.getEvent().getGuild().getRoleById(l);
                if (role != null) {
                    return role;
                }
            }
            return arg.findRole().orElse(null);
        });

        map.put(TEXTCHANNEL, (arg, flags) -> {
            if (TypeParserFlags.has(flags, "strict")) {
                if (arg.isValidTextChannel()) return arg.getTextChannel();
                else return null;
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                TextChannel channel = arg.getEvent().getJDA().getTextChannelById(l);
                if (channel != null) {
                    return channel;
                }
            }
            return arg.findTextChannel().orElse(null);
        });

        map.registerParameterTypeFlagless(EMOTE, CommandArgument::getEmote);

        map.registerParameterTypeFlagless(EMOJI, CommandArgument::getEmoji);

        map.registerParameterTypeFlagless(Duration.class, new DurationMapper());

        map.registerParameterTypeFlagless(OffsetDateTime.class, new DateTimeMapper());

        map.registerParameterTypeFlagless(String.class, CommandArgument::getArgument);
    }

}
