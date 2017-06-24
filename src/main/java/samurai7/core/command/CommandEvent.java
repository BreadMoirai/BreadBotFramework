/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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
 *
 */

package samurai7.core.command;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import org.assertj.core.util.Lists;
import samurai7.util.DiscordPatterns;
import samurai7.util.UnknownEmote;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This holds the context of a command including arguments.
 */
public abstract class CommandEvent extends Event {

    public CommandEvent(JDA api, long responseNumber) {
        super(api, responseNumber);
    }

    /**
     * The command prefix.
     */
    public abstract String getPrefix();

    /**
     * The command key.
     *
     * @return a {@link java.lang.String String}. May be empty.
     */
    public abstract  String getKey();

    /**
     * Whatever comes after the prefix and key.
     *
     * @return a {@link java.lang.String String} that does not contain the prefix or the key.
     * @see CommandEvent#getArgs()
     */
    public abstract  String getContent();

    /**
     * The {@link net.dv8tion.jda.core.entities.User User} who invoked the command.
     *
     * @see CommandEvent#getMember()
     */
    public abstract User getAuthor();

    public abstract long getAuthorId();

    /**
     * The {@link net.dv8tion.jda.core.entities.Member Member} who invoked the command.
     *
     * @see CommandEvent#getAuthor()
     */
    public abstract Member getMember();

    /**
     * The currently logged-in account as a {@link net.dv8tion.jda.core.entities.SelfUser SelfUser}.
     *
     * @return in this case, Samurai.
     * @see CommandEvent#getSelfMember()
     */
    public abstract SelfUser getSelfUser();

    /**
     * The currently logged-in account as a {@link net.dv8tion.jda.core.entities.Member Member} of the {@link net.dv8tion.jda.core.entities.Guild Guild} in which this command was invoked.
     * @return in this case, Samurai as a {@link net.dv8tion.jda.core.entities.Member Member}.
     * @see CommandEvent#getSelfUser()
     */
    public abstract Member getSelfMember();

    public abstract long getMessageId();

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} in which the command was invoked.
     * @return a Discord {@link net.dv8tion.jda.core.entities.Guild Guild}.
     */
    public abstract Guild getGuild();

    public abstract long getGuildId();

    /**
     * The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} in which the command was invoked.
     * @return a Discord {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     */
    public abstract TextChannel getChannel();

    public abstract long getChannelId();

    /**
     * The time this command was sent by the invoker.
     * @return a {@link java.time.OffsetDateTime OffsetDateTime}.
     * @see CommandEvent#getInstant()
     */
    public abstract OffsetDateTime getTime();

    /**
     * The time of which this command was invoked as an {@link java.time.Instant Instant}.
     * @return {@link CommandEvent#getTime() getTime()} as an {@link java.time.Instant Instant}.
     */
    public abstract Instant getInstant();

    /**
     * The core of the API.
     * @return {@link net.dv8tion.jda.core.JDA JDA}.
     */
    public abstract JDA getJDA();

    /**
     * This method is equivalent to {@link net.dv8tion.jda.core.entities.Message#getMentionedUsers() Message#getMentionedUsers()}
     */
    public abstract List<User> getMentionedUsers();

    /**
     * This method is equivalent to {@link net.dv8tion.jda.core.entities.Message#getMentionedRoles() Message#getMentionedRoles()}
     */
    public abstract List<Role> getMentionedRoles();

    /**
     * This method is equivalent to {@link net.dv8tion.jda.core.entities.Message#getMentionedChannels() Message#getMentionedChannels()}
     */
    public abstract List<TextChannel> getMentionedChannels();

    /**
     * This method is equivalent to {@link net.dv8tion.jda.core.entities.Message#getMentionedMembers() Message#getMentionedMembers()}
     */
    public abstract List<Member> getMentionedMembers();

    /**
     * Parses {@link CommandEvent#getContent() getContent()} as a list of arguments that are space delimited.
     * Code block formatting is stripped and no formatted content is passed such as Mentions.
     * Phrases contained within quotation marks are not separated.
     * <p>For example, if {@link CommandEvent#getContent() getContent()} returns
     * <pre>{@code hello, 1 23 "say no more"}</pre>
     * <p>Then this method will return a list with elements
     * <pre>{@code ["hello,", "1", "23", "say no more"]}</pre>
     * @return A mutable list of args. Every time this method is called {@link CommandEvent#getContent() getContent()} is parsed again and a new list is returned.
     */
    public List<String> getArgs() {
        return hasContent()
                ? Arrays.stream(DiscordPatterns.ARGUMENT_SPLITTER.split(getContent().replace('`', '\"'))).filter((s) -> !s.isEmpty()).filter(s -> !((s.startsWith("<") && s.endsWith(">")) || s.equals("@everyone") || s.equals("@here"))).map(s -> s.replace('\"', ' ')).map(String::trim).map(String::toLowerCase).collect(Collectors.toList())
                : Lists.emptyList();
    }

    /**
     * Parses {@link CommandEvent#getContent() getContent()} to find any custom emotes. If the bot is not in the guild with an emote used, An {@link samurai7.util.UnknownEmote UnknownEmote} will be added to the list instead. Only methods {@link net.dv8tion.jda.core.entities.Emote#getName() Emote#getName()}, {@link net.dv8tion.jda.core.entities.Emote#getId() Emote#getId()}, {@link net.dv8tion.jda.core.entities.Emote#getCreationTime() Emote#getCreationTime()}, and {@link net.dv8tion.jda.core.entities.Emote#getImageUrl() Emote#getImageUrl()} are supported. You can check for these with {@link net.dv8tion.jda.core.entities.Emote#isFake() Emote#isFake()}
     * @return A mutable list of args. Every time this method is called {@link CommandEvent#getContent() getContent()} is parsed again and a new list is returned.
     */
    public List<Emote> getEmotes() {
        List<Emote> emotes = new ArrayList<>();
        final Matcher emoteMatcher = DiscordPatterns.EMOTE_PATTERN.matcher(getContent());
        final JDA jda = getJDA();
        while (emoteMatcher.find()) {
            final String id = emoteMatcher.group(2);
            final Emote emoteById = jda.getEmoteById(id);
            if (emoteById != null)
                emotes.add(emoteById);
            else emotes.add(new UnknownEmote(emoteMatcher.group(1), Long.parseLong(id), jda));
        }
        return emotes;
    }

    /**
     * Checks for whether {@link CommandEvent#getContent() getContent()} will return an empty or null {@link java.lang.String String}
     * @return {@code true} if {@link CommandEvent#getContent() getContent()} is not empty. False otherwise.
     */
    public boolean hasContent() {
        return getContent() != null && !getContent().trim().isEmpty();
    }

    /**
     * @return only plain text and default emojis
     */
    public String getStrippedContent() {
        return getArgs().stream().filter(s -> !DiscordPatterns.FORMATTED.matcher(s).matches()).collect(Collectors.joining(" "));
    }

    /**
     *
     * @return
     */
    public String[] lines() {
        return DiscordPatterns.LINES.split(getContent());
    }

    public IntStream getIntArgs() {
        return Arrays.stream(getContent().split(" ")).flatMapToInt(this::parseIntArg);
    }

    public IntStream parseIntArg(String s) {
        try {
            final String[] split = s.split("-");
            if (split.length == 1) {
                if (isNumber(split[0]))
                    return IntStream.of(Integer.parseInt(split[0]));
            } else if (split.length == 2) {
                if (isNumber(split[0]) && isNumber(split[1])) {
                    final int a = Integer.parseInt(split[0]);
                    final int b = Integer.parseInt(split[1]);
                    if (a < b)
                        return IntStream.rangeClosed(a, b);
                    else return IntStream.rangeClosed(b, a).map(i -> a - i + b);

                }
            }
        } catch (NumberFormatException e) {
            return IntStream.empty();
        }
        return IntStream.empty();
    }

    public boolean isNumeric() {
        return isNumber(getContent());
    }

    public boolean isNumber(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), 10) < 0) return false;
        }
        return true;
    }

    public boolean isHex() {
        return DiscordPatterns.HEX.matcher(getContent()).matches();
    }

    public boolean isFloat() {
        return isFloat(getContent());
    }

    public boolean isFloat(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (i == 0 && c == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(c, 10) < 0 && c != '.') return false;
        }
        return true;
    }

    /**
     * @return a serializable implementation of ICommandContext.
     */
    public abstract CommandEvent serialize();
}
