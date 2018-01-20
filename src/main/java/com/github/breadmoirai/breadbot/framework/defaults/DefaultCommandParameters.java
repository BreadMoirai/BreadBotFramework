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

package com.github.breadmoirai.breadbot.framework.defaults;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CollectionTypes;
import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CommandParameterBuilderImpl;
import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CommandParameterTypeManagerImpl;
import com.github.breadmoirai.breadbot.util.DateTimeMapper;
import com.github.breadmoirai.breadbot.util.DurationMapper;
import com.github.breadmoirai.breadbot.util.Emoji;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Deque;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
        putParameterTypeParsers(map);

        putParameterTypeModifiers(map);
    }

    private void putParameterTypeParsers(CommandParameterTypeManagerImpl map) {
        map.putTypeParser(CommandArgument.class, arg -> arg);

        final TypeParser<Integer> intParser = (arg) -> {
            if (arg.isInteger()) {
                return arg.parseInt();
            } else {
                return null;
            }
        };
        map.put(INTEGER, intParser);
        map.put(Integer.class, intParser);


        final TypeParser<Long> longParser = (CommandArgument arg) -> {
            if (arg.isLong()) {
                return arg.parseLong();
            } else {
                return null;
            }
        };
        map.put(LONG, longParser);
        map.put(Long.class, longParser);


        final TypeParser<Float> floatParser = (arg) -> arg.isFloat() ? arg.parseFloat() : null;
        map.put(FLOAT, floatParser);
        map.put(Float.class, floatParser);


        final TypeParser<Double> doubleParser = (arg) -> arg.isFloat() ? arg.parseDouble() : null;
        map.put(DOUBLE, doubleParser);
        map.put(Double.class, doubleParser);


        final TypeParser<Boolean> boolParser = (arg) -> arg.isBoolean() ? arg.parseBoolean() : null;
        map.put(BOOLEAN, boolParser);
        map.put(Boolean.class, boolParser);

        map.put(RANGE, CommandArgument::parseRange);

        map.put(USER, (arg) -> {
            if (arg.isValidUser()) {
                return arg.getUser();
            } else {
                return arg.findMember().map(Member::getUser).orElseGet(() -> {
                    if (arg.isLong()) {
                        long l = arg.parseLong();
                        return arg.getEvent().getJDA().getUserById(l);
                    } else {
                        return null;
                    }
                });
            }
        });

        map.put(MEMBER, (arg) -> {
            if (arg.isValidMember()) {
                return arg.getMember();
            } else {
                return arg.findMember().orElseGet(() -> {
                    if (arg.isLong()) {
                        long l = arg.parseLong();
                        return arg.getEvent().getGuild().getMemberById(l);
                    } else {
                        return null;
                    }
                });
            }
        });

        map.put(ROLE, (arg) -> {
            if (arg.isValidRole()) {
                return arg.getRole();
            } else {
                return arg.findRole().orElseGet(() -> {
                    if (arg.isLong()) {
                        long l = arg.parseLong();
                        return arg.getEvent().getGuild().getRoleById(l);
                    } else {
                        return null;
                    }
                });
            }
        });

        map.put(TEXTCHANNEL, (arg) -> {
            if (arg.isValidTextChannel()) {
                return arg.getTextChannel();
            } else {
                return arg.findTextChannel().orElseGet(() -> {
                    if (arg.isLong()) {
                        long l = arg.parseLong();
                        return arg.getEvent().getGuild().getTextChannelById(l);
                    } else {
                        return null;
                    }
                });
            }
        });

        map.put(EMOTE, arg -> arg.isEmote() ? arg.getEmote() : null);

        map.put(EMOJI, arg -> arg.isEmoji() ? arg.getEmoji() : null);

        map.put(Duration.class, new DurationMapper());

        map.put(OffsetDateTime.class, new DateTimeMapper());

        map.put(String.class, CommandArgument::getArgument);
    }

    private void putParameterTypeModifiers(CommandParameterTypeManagerImpl map) {
        map.addTypeModifier(List.class, p -> {
            final CommandParameterBuilderImpl pc = (CommandParameterBuilderImpl) p;
            final Class<?> genericType = pc.getGenericType();
            CollectionTypes.setParserToGenericList(genericType, pc);
        });

        map.addTypeModifier(Deque.class, p -> {
            final CommandParameterBuilderImpl pc = (CommandParameterBuilderImpl) p;
            final Class<?> genericType = pc.getGenericType();
            CollectionTypes.setParserToGenericDeque(genericType, pc);
        });

        map.addTypeModifier(Stream.class, p -> {
            final CommandParameterBuilderImpl pc = (CommandParameterBuilderImpl) p;
            final Class<?> genericType = pc.getGenericType();
            CollectionTypes.setParserToGenericStream(genericType, pc);
        });

        map.addTypeModifier(IntStream.class, builder -> CollectionTypes.setParserToIntStream(((CommandParameterBuilderImpl) builder)));
        map.addTypeModifier(LongStream.class, builder -> CollectionTypes.setParserToLongStream(((CommandParameterBuilderImpl) builder)));
        map.addTypeModifier(DoubleStream.class, builder -> CollectionTypes.setParserToDoubleStream(((CommandParameterBuilderImpl) builder)));

        map.addTypeModifier(CommandEvent.class, p -> p.setParser((parameter, list, parser) -> parser.getEvent()));

        map.addTypeModifier(Message.class, p -> p.setParser((parameter, list, parser) -> parser.getEvent().getMessage()));
        map.addTypeModifier(Guild.class, p -> p.setParser((parameter, list, parser) -> parser.getEvent().getGuild()));
        map.addTypeModifier(TextChannel.class, p -> p.setParser((parameter, list, parser) -> parser.getEvent().getChannel()));
        map.addTypeModifier(MessageChannel.class, p -> p.setParser((parameter, list, parser) -> parser.getEvent().getChannel()));
        map.addTypeModifier(Message.Attachment.class, p -> p.setParser((parameter, list, parser) -> parser.getEvent().getMessage().getAttachments().stream().findFirst().orElse(null)));
    }

}
