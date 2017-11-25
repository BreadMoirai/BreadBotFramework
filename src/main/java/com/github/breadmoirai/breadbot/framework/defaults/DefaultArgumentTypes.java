package com.github.breadmoirai.breadbot.framework.defaults;

import com.github.breadmoirai.breadbot.framework.internal.argument.ArgumentTypesManagerImpl;
import com.github.breadmoirai.breadbot.framework.internal.parameter.*;
import com.github.breadmoirai.breadbot.util.Arguments;
import com.github.breadmoirai.breadbot.util.DateTimeMapper;
import com.github.breadmoirai.breadbot.util.DurationMapper;
import com.github.breadmoirai.breadbot.util.Emoji;
import net.dv8tion.jda.core.entities.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.stream.IntStream;

public class DefaultArgumentTypes {
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

    public void initialize(ArgumentTypesManagerImpl map) {
        final ArgumentTypePredicate intPredicate = (arg, flags) -> {
            final boolean hex = ArgumentFlags.hasFlag(flags, ArgumentFlags.HEX);
            return hex ? arg.isHex() : arg.isInteger();
        };
        final ArgumentTypeMapper<Integer> intType = (arg, flags) -> {
            final boolean hex = ArgumentFlags.hasFlag(flags, ArgumentFlags.HEX);
            if (hex) {
                try {
                    return Integer.parseInt(Arguments.stripHexPrefix(arg.getArgument()), 16);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            } else {
                return arg.parseInt();
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
                    return Long.parseLong(Arguments.stripHexPrefix(arg.getArgument()), 16);
                } catch (NumberFormatException ignored) {

                }
            } else if (arg.isLong()) {
                return arg.parseLong();
            }
            return null;
        });
        map.put(LONG, longParser);
        map.put(Long.class, longParser);

        map.registerArgumentMapperSimple(FLOAT, CommandArgument::isFloat, CommandArgument::parseFloat);
        map.registerArgumentMapperSimple(Float.class, CommandArgument::isFloat, CommandArgument::parseFloat);

        map.registerArgumentMapperSimple(DOUBLE, CommandArgument::isFloat, CommandArgument::parseDouble);
        map.registerArgumentMapperSimple(Double.class, CommandArgument::isFloat, CommandArgument::parseDouble);


        map.registerArgumentMapperSimple(BOOLEAN, CommandArgument::isBoolean, CommandArgument::parseBoolean);
        map.registerArgumentMapperSimple(Boolean.class, CommandArgument::isBoolean, CommandArgument::parseBoolean);

        map.registerArgumentMapper(RANGE, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                return arg.isRange();
            } else return arg.isInteger() || arg.isRange();
        }, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                return arg.isNumeric() ?null : arg.parseRange();
            } else return arg.parseRange();
        });

        map.registerArgumentMapper(USER, null, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                if (arg.isValidUser()) return arg.getUser();
                else return null;
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                return arg.getEvent().getJDA().getUserById(l);
            }
            return arg.findMember().map(Member::getUser).orElse(null);
        });

        map.registerArgumentMapper(MEMBER, null, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
                if (arg.isValidMember()) return arg.getMember();
                else return null;
            }
            if (arg.isLong()) {
                long l = arg.parseLong();
                return arg.getEvent().getGuild().getMemberById(l);
            }
            return arg.findMember().orElse(null);
        });

        map.registerArgumentMapper(ROLE, null, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
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

        map.registerArgumentMapper(TEXTCHANNEL, null, (arg, flags) -> {
            if (ArgumentFlags.isStrict(flags)) {
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

        map.registerArgumentMapperSimple(EMOTE, CommandArgument::isEmote, CommandArgument::getEmote);

        map.registerArgumentMapperSimple(EMOJI, CommandArgument::isEmoji, CommandArgument::getEmoji);

        map.registerArgumentMapperSimple(Duration.class, null, new DurationMapper());

        map.registerArgumentMapperSimple(OffsetDateTime.class, null, new DateTimeMapper());

        map.registerArgumentMapperSimple(String.class, null, CommandArgument::getArgument);
    }

}
