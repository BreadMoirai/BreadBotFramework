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
 */
package com.github.breadmoirai.bot.framework.core;

import com.github.breadmoirai.bot.framework.core.response.menu.PromptBuilder;
import com.github.breadmoirai.bot.framework.core.response.menu.ReactionMenuBuilder;
import com.github.breadmoirai.bot.framework.core.response.simple.EmbedResponse;
import com.github.breadmoirai.bot.framework.core.response.simple.MessageResponse;
import com.github.breadmoirai.bot.framework.core.response.simple.ReactionResponse;
import com.github.breadmoirai.bot.framework.core.response.simple.StringResponse;
import com.github.breadmoirai.bot.framework.util.DiscordPatterns;
import com.github.breadmoirai.bot.framework.util.UnknownEmote;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This holds the context of a command including arguments.
 */
public abstract class CommandEvent extends Event {

    private final SamuraiClient client;

    public CommandEvent(JDA api, long responseNumber, SamuraiClient client) {
        super(api, responseNumber);
        this.client = client;
    }

    public SamuraiClient getClient() {
        return client;
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

    public abstract Message getMessage();

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
     * This will retrieve ass the users mentioned in a message as members. If a user mentioned is not part of the guild, it will not be included.
     */
    public abstract List<Member> getMentionedMembers();

    /**
     * Parses {@link CommandEvent#getContent() getContent()} as a list of arguments that are space delimited.
     * Code block formatting is stripped and no formatted content is passed such as Mentions.
     * Phrases contained within quotation marks are not separated.
     * Formatted input and mentions are ignored.
     * <p>For example, if {@link CommandEvent#getContent() getContent()} returns
     * <pre>{@code hello, 1 23 <@12341482523> "say no more"}</pre>
     * <p>Then this method will return a list with elements
     * <pre>{@code ["hello,", "1", "23", "say no more"]}</pre>
     * @return A mutable list of args. Every time this method is called {@link CommandEvent#getContent() getContent()} is parsed again and a new list is returned.
     */
    public List<String> getArgs() {
        return hasContent()
                ? Arrays.stream(DiscordPatterns.ARGUMENT_SPLITTER.split(getContent().replace('`', '\"'))).filter((s) -> !s.isEmpty()).filter(s -> !((s.startsWith("<") && s.endsWith(">")) || s.equals("@everyone") || s.equals("@here"))).map(s -> s.replace('\"', ' ')).map(String::trim).map(String::toLowerCase).collect(Collectors.toList())
                : Collections.emptyList();
    }

    /**
     * Parses {@link CommandEvent#getContent() getContent()} to find any custom emotes. If the bot is not in the guild with an emote used, An {@link UnknownEmote UnknownEmote} will be added to the list instead. Only methods {@link net.dv8tion.jda.core.entities.Emote#getName() Emote#getName()}, {@link net.dv8tion.jda.core.entities.Emote#getId() Emote#getId()}, {@link net.dv8tion.jda.core.entities.Emote#getCreationTime() Emote#getCreationTime()}, and {@link net.dv8tion.jda.core.entities.Emote#getImageUrl() Emote#getImageUrl()} are supported. You can check for these with {@link net.dv8tion.jda.core.entities.Emote#isFake() Emote#isFake()}
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


    public abstract void reply(String message);
    public abstract void reply(MessageEmbed message);
    public abstract void reply(Message message);
    public abstract void replyPrivate(String message);
    public abstract void replyPrivate(MessageEmbed message);
    public abstract void replyPrivate(Message message);
    public abstract void replyReaction(Emote emote);
    public abstract void replyReaction(String emoji);

    public void replyFormat(String format, Object... args) {
        reply(String.format(format, args));
    }

    public StringResponse respond(String message) {
        final StringResponse r = new StringResponse(message);
        r.base(this);
        return r;
    }

    public EmbedResponse respond(MessageEmbed message){
        final EmbedResponse r = new EmbedResponse(message);
        r.base(this);
        return r;
    }

    public MessageResponse respond(Message message){
        final MessageResponse r = new MessageResponse(message);
        r.base(this);
        return r;
    }

    public ReactionResponse respondReaction(Emote emote){
        final ReactionResponse r = new ReactionResponse(getMessageId(), emote);
        r.base(this);
        return r;
    }

    public ReactionResponse respondReaction(String emoji){
        final ReactionResponse r = new ReactionResponse(getMessageId(), emoji);
        r.base(this);
        return r;
    }


    public ReactionMenuBuilder respondReactionMenu() {
        final ReactionMenuBuilder r = new ReactionMenuBuilder();
        r.base(this);
        return r;
    }

    public PromptBuilder respondPrompt() {
        final PromptBuilder r = new PromptBuilder();
        r.base(this);
        return r;
    }

    public void replyWith(Response r) {
        r.base(this);
        client.send(this.getChannel(), r);
    }

    /**
     * This splits the message into lines separated via "\n"
     * @return an array of Strings
     */
    public String[] lines() {
        return DiscordPatterns.LINES.split(getContent());
    }

    /**
     * Retrieves the contents of the message as Ints. A message of {@code "hello 1 3 9-6 4-5 what 30 20} will return an IntStream with the elements of {@code [1,3,9,8,7,6,4,5,30,20]}.
     * If a number or range of numbers fall above the maximum size of an Integer, it will not be included in the returned IntStream.
     * @return {@link java.util.stream.IntStream} of ints in the order declared by user.
     * <p> if such is the case that there are no integers within the message, an Empty IntStream is returned.</p>
     */
    @NotNull
    public IntStream getIntArgs() {
        return getContent() == null ? IntStream.empty() : Arrays.stream(getContent().split(" ")).flatMapToInt(CommandEvent::parseIntArg);
    }

    public static IntStream parseIntArg(String s) {
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

    public static boolean isNumber(String s) {
        if (s == null) return false;
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
        return isHex(getContent());
    }

    public static boolean isHex(String s) {
        return DiscordPatterns.HEX.matcher(s).matches();
    }

    public boolean isFloat() {
        return isFloat(getContent());
    }

    public static boolean isFloat(String s) {
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
     * Do not use. This is a work in progress
     * @return shit.
     */
    public abstract CommandEvent serialize();
}
