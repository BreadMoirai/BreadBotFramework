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
package com.github.breadmoirai.bot.framework.event;

import com.github.breadmoirai.bot.framework.CommandClient;
import com.github.breadmoirai.bot.framework.Response;
import com.github.breadmoirai.bot.framework.command.arg.CommandArgument;
import com.github.breadmoirai.bot.framework.command.arg.CommandArgumentList;
import com.github.breadmoirai.bot.framework.response.menu.PromptBuilder;
import com.github.breadmoirai.bot.framework.response.menu.ReactionMenuBuilder;
import com.github.breadmoirai.bot.framework.response.simple.EmbedResponse;
import com.github.breadmoirai.bot.framework.response.simple.MessageResponse;
import com.github.breadmoirai.bot.framework.response.simple.ReactionResponse;
import com.github.breadmoirai.bot.framework.response.simple.StringResponse;
import com.github.breadmoirai.bot.util.DiscordPatterns;
import com.github.breadmoirai.bot.util.MissingPermissionResponse;
import com.github.breadmoirai.bot.util.UnknownEmote;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This holds the context of a command including arguments.
 */
public abstract class CommandEvent extends Event {

    private static int DEFAULT_LIMIT = -1;
    private CommandArgumentList argumentList;

    public static void setDefaultArgumentLimit(int limit) {
        DEFAULT_LIMIT = limit;
    }

    private final CommandClient client;
    private List<String> args;

    public CommandEvent(JDA api, long responseNumber, CommandClient client) {
        super(api, responseNumber);
        this.client = client;
    }

    public CommandClient getClient() {
        return client;
    }

    /**
     * The command prefix.
     */
    public abstract String getPrefix();

    /**
     * The command key. The case is not guaranteed.
     *
     * @return a {@link java.lang.String String}. May be empty.
     */
    public abstract String getKey();

    /**
     * Whatever comes after the prefix and key.
     *
     * @return a {@link java.lang.String String} that does not contain the prefix or the key.
     * @see CommandEvent#getArgs()
     */
    public abstract String getContent();

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
     *
     * @return in this case, Samurai as a {@link net.dv8tion.jda.core.entities.Member Member}.
     * @see CommandEvent#getSelfUser()
     */
    public abstract Member getSelfMember();

    public abstract long getMessageId();

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} in which the command was invoked.
     *
     * @return a Discord {@link net.dv8tion.jda.core.entities.Guild Guild}.
     */
    public abstract Guild getGuild();

    public abstract long getGuildId();

    /**
     * The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} in which the command was invoked.
     *
     * @return a Discord {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     */
    public abstract TextChannel getChannel();

    public abstract long getChannelId();

    /**
     * The time this command was sent by the invoker.
     *
     * @return a {@link java.time.OffsetDateTime OffsetDateTime}.
     * @see CommandEvent#getInstant()
     */
    public abstract OffsetDateTime getTime();

    /**
     * The time of which this command was invoked as an {@link java.time.Instant Instant}.
     *
     * @return {@link CommandEvent#getTime() getTime()} as an {@link java.time.Instant Instant}.
     */
    public abstract Instant getInstant();

    /**
     * The core of the API.
     *
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
     * I think {@link CommandEvent#getArguments(int)} is more useful than this. idk.
     * <p>
     * <p>Parses {@link CommandEvent#getContent() getContent()} as a list of arguments that are space delimited.
     * Code block formatting is stripped and no formatted content is passed such as Mentions.
     * Phrases contained within quotation marks are not separated.
     * Formatted input and mentions are ignored.
     * If message content contains an uneven number of {@code "}, the result is not predictable.
     * <p>
     * <p>For example, if {@link CommandEvent#getContent() getContent()} returns
     * <pre>{@code hello, 1 23 <@12341482523> "say no more" @everyone}</pre>
     * <p>Then this method will return a list with elements
     * <pre>{@code ["hello,", "1", "23", "say no more"]}</pre>
     *
     * @return An immutable list of args.
     */
    public synchronized List<String> getArgs() {
        if (args == null) {
            args = hasContent()
                    ? Arrays.stream(DiscordPatterns.ARGUMENT_SPLITTER.split(getContent().replace('`', '\"'))).filter((s) -> !s.isEmpty()).filter(s -> !((s.startsWith("<") && s.endsWith(">")) || s.equals("@everyone") || s.equals("@here"))).map(s -> s.replace('\"', ' ')).map(String::trim).map(String::toLowerCase).collect(Collectors.toList())
                    : Collections.emptyList();
        }
        return args;
    }

    /**
     * @return the number of arguments provided.
     */
    public int getArgumentCount() {
        return getArguments().size();
    }

    /**
     * retrieves a {@link com.github.breadmoirai.bot.framework.command.arg.CommandArgument} from the {@link com.github.breadmoirai.bot.framework.command.arg.CommandArgumentList} returned by {@link com.github.breadmoirai.bot.framework.event.CommandEvent#getArguments()}
     *
     * @param index the index of the argument starting at 0. This does not include the key.
     * @return the non-null CommandArgument
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than {@link com.github.breadmoirai.bot.framework.event.CommandEvent#getArgumentCount()}
     */
    public CommandArgument getArgumentAt(int index) {
        return getArguments().get(index);
    }

    /**
     * Calls {@link com.github.breadmoirai.bot.framework.event.CommandEvent#getArguments(int)} with the default limit.
     *
     * @return a CommandArgumentList
     * @see com.github.breadmoirai.bot.framework.event.CommandEvent#getArguments(int)
     * @see com.github.breadmoirai.bot.framework.event.CommandEvent#setDefaultArgumentLimit
     */
    public CommandArgumentList getArguments() {
        return getArguments(DEFAULT_LIMIT);
    }

    /**
     * <p>Parses {@link CommandEvent#getContent() getContent()} as a list of arguments that are space delimited.
     * Arguments enclosed in quotes will be returned as a single argument.
     * If message content contains an uneven number of {@code "}, the result is not predictable.
     *
     * @param limit the limit to set for a maximum number of arguments. For more information on how this is used, see {@link java.util.regex.Pattern#split(java.lang.CharSequence, int)}
     * @return an implementation of <code>{@link java.util.List}<{@link CommandArgument EventArgument}></code> in which arguments are lazily parsed.
     */
    public synchronized CommandArgumentList getArguments(int limit) {
        if (!hasContent()) {
            if (argumentList == null) {
                argumentList = new CommandArgumentList(new String[]{}, this);
            }
            return argumentList;
        }
        if (limit == DEFAULT_LIMIT) {
            if (argumentList == null) {
                argumentList = createNewArgumentList(DEFAULT_LIMIT);
            }
            return argumentList;
        }
        return createNewArgumentList(limit);
    }

    /**
     * Creates a new list of arguments using the provided regex to split the message contents.
     *
     * @param regex the regex to split on
     * @param limit the split limit
     *
     * @return a new {@link com.github.breadmoirai.bot.framework.command.arg.CommandArgumentList}.
     *
     * @see java.util.regex.Pattern#split(java.lang.CharSequence, int)
     */
    @NotNull
    public CommandArgumentList createNewArgumentList(String regex, int limit) {
        return createNewArgumentList(Pattern.compile(regex), limit);
    }

    /**
     * Creates a new list of arguments using the default regex to split the message contents.
     * recommended to use {@link com.github.breadmoirai.bot.framework.event.CommandEvent#getArguments(int)} instead.
     *
     * @param limit the split limit
     *
     * @return a new {@link com.github.breadmoirai.bot.framework.command.arg.CommandArgumentList}.
     *
     * @see java.util.regex.Pattern#split(java.lang.CharSequence, int)
     * @see com.github.breadmoirai.bot.util.DiscordPatterns#ARGUMENT_SPLITTER
     */
    @NotNull
    public CommandArgumentList createNewArgumentList(int limit) {
        return createNewArgumentList(DiscordPatterns.ARGUMENT_SPLITTER, limit);
    }

    /**
     * Creates a new list of arguments using the provided regex to split the message contents.
     *
     * @param splitter the pattern to split on
     * @param limit the split limit
     *
     * @return a new {@link com.github.breadmoirai.bot.framework.command.arg.CommandArgumentList}.
     *
     * @see java.util.regex.Pattern#split(java.lang.CharSequence, int)
     */
    @NotNull
    public CommandArgumentList createNewArgumentList(Pattern splitter, int limit) {
        final String[] split = splitter.split(getContent().replace('`', '\"'), limit);
        final String[] strings = Arrays.stream(split)
                .map(s -> s.replace('\"', ' '))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toArray(String[]::new);
        return new CommandArgumentList(strings, this);
    }

    /**
     * Parses {@link CommandEvent#getContent() getContent()} to find any custom emotes. If the bot is not in the guild with an emote used, An {@link UnknownEmote UnknownEmote} will be added to the list instead. Only methods {@link net.dv8tion.jda.core.entities.Emote#getName() Emote#getName()}, {@link net.dv8tion.jda.core.entities.Emote#getId() Emote#getId()}, {@link net.dv8tion.jda.core.entities.Emote#getCreationTime() Emote#getCreationTime()}, and {@link net.dv8tion.jda.core.entities.Emote#getImageUrl() Emote#getImageUrl()} are supported. You can check for these with {@link net.dv8tion.jda.core.entities.Emote#isFake() Emote#isFake()}
     *
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
     *
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

    public EmbedResponse respond(MessageEmbed message) {
        final EmbedResponse r = new EmbedResponse(message);
        r.base(this);
        return r;
    }

    public MessageResponse respond(Message message) {
        final MessageResponse r = new MessageResponse(message);
        r.base(this);
        return r;
    }

    public ReactionResponse respondReaction(Emote emote) {
        final ReactionResponse r = new ReactionResponse(getMessageId(), emote);
        r.base(this);
        return r;
    }

    public ReactionResponse respondReaction(String emoji) {
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

    @Override
    public String toString() {
        return String.format("CommandEvent{ Guild=%d, Channel=%d, Author=%d, Content=%s }", getGuildId(), getChannelId(), getAuthorId(), getContent());
    }

    /**
     * Checks to see if the bot has the permissions required.
     *
     * @param permission any permissions required.
     *
     * @return {@code true} if the bot has the permissions required. {@code false} otherwise.
     */
    public boolean checkPermission(Permission... permission) {
        return PermissionUtil.checkPermission(getChannel(), getSelfMember(), permission);
    }

    /**
     * Checks to see if the bot has the permissions required. If the permissions required are not found, the user is notified with a {@link com.github.breadmoirai.bot.util.MissingPermissionResponse}.
     *
     * @param permission any permissions required.
     *
     * @return {@code true} if the bot has the permissions required. {@code false} otherwise.
     */
    public boolean requirePermission(Permission... permission) {
        if (!checkPermission(permission)) {
            replyWith(new MissingPermissionResponse(this, permission));
            return false;
        }
        return true;
    }
}