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

package com.github.breadmoirai.breadbot.framework.event;

import com.github.breadmoirai.breadbot.framework.BreadBot;
import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandArgumentArrayList;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandArgumentLazyArrayList;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandArgumentLazyLinkedList;
import com.github.breadmoirai.breadbot.framework.event.internal.DefaultArgumentSplitterImpl;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.response.RestActionExtension;
import com.github.breadmoirai.breadbot.framework.response.internal.CommandResponseMessage;
import com.github.breadmoirai.breadbot.util.MissingPermission;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.exceptions.VerificationLevelException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.ErrorResponse;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import net.dv8tion.jda.core.utils.PermissionUtil;

import javax.annotation.CheckReturnValue;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This holds the context of a command including arguments.
 */
@SuppressWarnings("unused")
public abstract class CommandEvent extends Event {

    private static int DEFAULT_LIMIT = -1;
    private final BreadBot client;
    private final boolean isHelpEvent;
    protected CommandArgumentList argumentList;

    public CommandEvent(JDA api, long responseNumber, BreadBot client, boolean isHelpEvent) {
        super(api, responseNumber);
        this.client = client;
        this.isHelpEvent = isHelpEvent;
    }

    public static void setDefaultArgumentLimit(int limit) {
        DEFAULT_LIMIT = limit;
    }

    public BreadBot getClient() {
        return client;
    }

    /**
     * retrieves the command associated with this event.
     *
     * @return a non-null CommandHandle
     */
    public abstract Command getCommand();

    /**
     * The prefix set for this guild
     *
     * @return The string as provided by the set PrefixModule
     */
    public abstract String getPrefix();

    /**
     * The command key. The case is as was sent. Guaranteed to be as least of length 1. Not Null.
     *
     * @return a {@link java.lang.String String}. May be empty.
     */
    public abstract String[] getKeys();

    /**
     * Retrieves the key that corresponds to this command.
     *
     * @return a {@link String} representing the key as it was sent by the user.
     */
    public String getKey() {
        final String[] keys = getKeys();
        if (keys.length == 1) {
            return keys[0];
        } else {
            final int i = countDepth(getCommand());
            return keys[i - 1];
        }
    }

    private int countDepth(Command command) {
        if (command.getParent() != null) {
            return 1 + countDepth(command.getParent());
        }
        return 1;
    }

    /**
     * Whatever comes after the prefix and the keys.
     *
     * @return a {@link java.lang.String String} that does not contain the prefix or any of the keys.
     *
     * @see CommandEvent#getArguments()
     */
    public abstract String getContent();

    public abstract Message getMessage();

    /**
     * The {@link net.dv8tion.jda.core.entities.User User} who invoked the command.
     *
     * @return the author of the message
     *
     * @see CommandEvent#getMember()
     */
    public abstract User getAuthor();

    public abstract long getAuthorId();

    /**
     * The {@link net.dv8tion.jda.core.entities.Member Member} who invoked the command.
     *
     * @return the author of the message
     *
     * @see CommandEvent#getAuthor()
     */
    public abstract Member getMember();

    /**
     * The currently logged-in account as a {@link net.dv8tion.jda.core.entities.SelfUser SelfUser}.
     *
     * @return in this case, Samurai.
     *
     * @see CommandEvent#getSelfMember()
     */
    public abstract SelfUser getSelfUser();

    /**
     * The currently logged-in account as a {@link net.dv8tion.jda.core.entities.Member Member} of the {@link
     * net.dv8tion.jda.core.entities.Guild Guild} in which this command was invoked.
     *
     * @return in this case, Samurai as a {@link net.dv8tion.jda.core.entities.Member Member}.
     *
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
     * The jda
     *
     * @return {@link net.dv8tion.jda.core.JDA JDA}.
     */
    public abstract JDA getJDA();

    /**
     * @return the number of arguments provided.
     */
    public int getArgumentCount() {
        return getArguments().size();
    }

    /**
     * retrieves a {@link CommandArgument} from the {@link CommandArgumentList} returned by {@link
     * CommandEvent#getArguments()}
     *
     * @param index
     *         the index of the argument starting at 0. This does not include the key.
     *
     * @return the non-null CommandArgument
     *
     * @throws IndexOutOfBoundsException
     *         if the index is less than 0 or greater than {@link CommandEvent#getArgumentCount()}
     */
    public CommandArgument getArgumentAt(int index) {
        return getArguments().get(index);
    }

    /**
     * Calls {@link CommandEvent#getArguments(ArgumentSplitter)} with the default splitter on spaces unless enclosed
     * with quotes or backticks.
     *
     * @return a CommandArgumentList
     *
     * @see CommandEvent#getArguments(ArgumentSplitter)
     * @see CommandEvent#setDefaultArgumentLimit
     */
    public CommandArgumentList getArguments() {
        return getArguments(null);
    }

    /**
     * <p>Parses {@link CommandEvent#getContent() getContent()} according to the splitter.
     *
     * @param splitter
     *         an {@link ArgumentSplitter} that provides an iterator, separating this into arguments
     *
     * @return an implementation of {@link java.util.List}<{@link CommandArgument EventArgument}>
     */
    public synchronized CommandArgumentList getArguments(ArgumentSplitter splitter) {
        if (splitter == null && hasContent()) {
            if (argumentList == null) {
                final Iterator<String> arguments = new DefaultArgumentSplitterImpl().getArguments(getContent());
                argumentList = new CommandArgumentLazyLinkedList(arguments, this);
            }
            return argumentList;
        }
        if (!hasContent()) {
            if (argumentList == null) {
                argumentList = new CommandArgumentArrayList(new CommandArgument[]{}, this);
            }
            return argumentList;
        }
        //noinspection ConstantConditions splitter is never null here, intelliJ get your shit together
        final Iterator<String> arguments = splitter.getArguments(getContent());
        return new CommandArgumentLazyLinkedList(arguments, this);
    }

    /**
     * Creates a new list of arguments using the provided regex to split the message contents.
     *
     * @param regex
     *         the regex to split on
     * @param limit
     *         the split limit
     *
     * @return a new {@link CommandArgumentList}.
     *
     * @see java.util.regex.Pattern#split(java.lang.CharSequence, int)
     */
    public CommandArgumentList createNewArgumentList(String regex, int limit) {
        return createNewArgumentList(Pattern.compile(regex), limit);
    }

    /**
     * Creates a new list of arguments using the provided regex to split the message contents.
     *
     * @param splitter
     *         the pattern to split on
     * @param limit
     *         the split limit
     *
     * @return a new {@link CommandArgumentList}.
     *
     * @see java.util.regex.Pattern#split(java.lang.CharSequence, int)
     */
    public CommandArgumentList createNewArgumentList(Pattern splitter, int limit) {
        final String[] split = splitter.split(getContent().replace('`', '\"'), limit);
        final String[] strings = Arrays.stream(split)
                .map(s -> s.replace('\"', ' '))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        return new CommandArgumentLazyArrayList(strings, this);
    }

    /**
     * Checks for whether {@link CommandEvent#getContent() getContent()} will return an empty or null {@link
     * java.lang.String String}
     *
     * @return {@code true} if {@link CommandEvent#getContent() getContent()} is not empty. False otherwise.
     */
    public boolean hasContent() {
        return getContent() != null && !getContent().trim().isEmpty();
    }

    /**
     * Provides a fluent style message builder that allows you to set the success consumer and/or attach a file. Please
     * note: You must use #send() or #build() in order to send the message.
     *
     * @return a builder for a message that is fully featured
     */
    public abstract CommandResponseMessage.RMessageBuilder reply();

    /**
     * Provides a fluent style message builder that allows you to set the success consumer and/or attach a file. Please
     * note: You must use #send() or #build() in order to send the message.
     *
     * @return a builder for a message that is fully featured
     */
    public abstract CommandResponseMessage.RMessageBuilder reply(String message);

    /**
     * Provides a fluent style builder for the success consumer and failure consumer. Please note: You must use #send()
     * or #build() in order to send the message.
     *
     * @return a builder form of the RestAction
     */
    public abstract RestActionExtension<Message> reply(Message message);

    public abstract RestActionExtension<Void> replyReaction(Emote emote);

    public abstract RestActionExtension<Void> replyReaction(String emoji);

    public CommandResponseMessage.RMessageBuilder replyFormat(String format, Object... args) {
        return reply(String.format(format, args));
    }

    /**
     * Sends a plain text message to this channel.
     * <br>This will fail if this channel is an instance of {@link TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link TextChannel TextChannel}
     * use {@link TextChannel#canTalk() TextChannel.canTalk()}.
     *
     * <p>This method is a shortcut to {@link #sendMessage(Message)} by way of using a
     * {@link MessageBuilder MessageBuilder} internally to build the provided {@code text} into a Message.
     * <pre>sendMessage(new MessageBuilder().append(text).build())</pre>
     *
     * <p>For {@link ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param text
     *         the text to build into a Message to send to the MessageChannel.
     *
     * @return {@link MessageAction MessageAction}
     * <br>The newly created Message after it has been sent to Discord.
     *
     * @throws InsufficientPermissionException
     *         If this is a {@link TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *         <li>{@link Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *         <li>{@link Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *         </ul>
     * @throws VerificationLevelException
     *         If this is a {@link TextChannel} and {@link TextChannel#getGuild() TextChannel.getGuild()}{@link
     *         Guild#checkVerification() .checkVerification()} returns false.
     * @throws IllegalArgumentException
     *         if the provided text is null, empty or longer than 2000 characters
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel PrivateChannel} and both the currently logged in account and the
     *         target user are bots.
     * @see MessageBuilder
     */
    @CheckReturnValue
    public MessageAction sendMessage(CharSequence text) {return getChannel().sendMessage(text);}

    /**
     * Sends a formatted text message to this channel.
     * <br>This will fail if this channel is an instance of {@link TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link TextChannel TextChannel}
     * use {@link TextChannel#canTalk() TextChannel.canTalk()}.
     *
     * <p>This method is a shortcut to {@link #sendMessage(Message)} by way of using a
     * {@link MessageBuilder MessageBuilder} and using its {@link MessageBuilder#appendFormat(String, Object...)}
     * method.
     * <br>For more information on how to format your input, refer to the docs of the method mentioned above.
     *
     * <p>For {@link ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param format
     *         The string that should be formatted, if this is {@code null} or empty the content of the Message would be
     *         empty and cause a builder exception.
     * @param args
     *         The arguments for your format
     *
     * @return {@link MessageAction MessageAction}
     * <br>The newly created Message after it has been sent to Discord.
     *
     * @throws InsufficientPermissionException
     *         If this is a {@link TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *         <li>{@link Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *         <li>{@link Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *         </ul>
     * @throws VerificationLevelException
     *         If this is a {@link TextChannel} and {@link TextChannel#getGuild() TextChannel.getGuild()}{@link
     *         Guild#checkVerification() .checkVerification()} returns false.
     * @throws IllegalArgumentException
     *         If the provided format text is {@code null}, empty or longer than 2000 characters
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel PrivateChannel} and both the currently logged in account and the
     *         target user are bots.
     * @throws IllegalFormatException
     *         If a format string contains an illegal syntax, a format specifier that is incompatible with the given
     *         arguments, insufficient arguments given the format string, or other illegal conditions. For specification
     *         of all possible formatting errors, see the <a href="../util/Formatter.html#detail">Details</a> section of
     *         the formatter class specification.
     */
    @CheckReturnValue
    public MessageAction sendMessageFormat(String format, Object... args) {
        return getChannel().sendMessageFormat(format, args);
    }

    /**
     * Sends a specified {@link MessageEmbed MessageEmbed} as a {@link Message Message} to this channel.
     * <br>This will fail if this channel is an instance of {@link TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link TextChannel TextChannel}
     * use {@link TextChannel#canTalk() TextChannel#canTalk}.
     *
     * <p>This method is a shortcut to {@link #sendMessage(Message)} by way of using a
     * {@link MessageBuilder MessageBuilder} internally to build the provided {@code embed} into a Message.
     * <pre>sendMessage(new MessageBuilder().setEmbed(embed).build())</pre>
     *
     * <p>For {@link ErrorResponse} information, refer to {@link #sendMessage(Message)}.
     *
     * @param embed
     *         the {@link MessageEmbed MessageEmbed} to send
     *
     * @return {@link MessageAction MessageAction}
     * <br>The newly created Message after it has been sent to Discord.
     *
     * @throws InsufficientPermissionException
     *         If this is a {@link TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *         <li>{@link Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *         <li>{@link Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *         <li>{@link Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS}</li>
     *         </ul>
     * @throws VerificationLevelException
     *         If this is a {@link TextChannel} and {@link TextChannel#getGuild() TextChannel.getGuild()}{@link
     *         Guild#checkVerification() .checkVerification()} returns false.
     * @throws IllegalArgumentException
     *         If the provided embed is {@code null} or if the provided {@link MessageEmbed MessageEmbed} is not {@link
     *         MessageEmbed#isSendable(AccountType) sendable}
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel PrivateChannel} and both the currently logged in account and the
     *         target user are bots.
     * @see MessageBuilder
     * @see EmbedBuilder
     */
    @CheckReturnValue
    public MessageAction sendMessage(
            MessageEmbed embed) {return getChannel().sendMessage(embed);}

    /**
     * Sends a specified {@link Message Message} to this channel.
     * <br>This will fail if this channel is an instance of {@link TextChannel TextChannel} and
     * the currently logged in account does not have permissions to send a message to this channel.
     * <br>To determine if you are able to send a message in a {@link TextChannel TextChannel}
     * use {@link TextChannel#canTalk() TextChannel#canTalk}.
     *
     * <p>The following {@link ErrorResponse ErrorResponses} are possible:
     * <ul>
     * <li>{@link ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     * <br>The request was attempted after the account lost access to the
     * {@link Guild Guild} or {@link Group Group} typically due to being kicked or removed, or after {@link
     * Permission#MESSAGE_READ Permission.MESSAGE_READ} was revoked in the {@link TextChannel TextChannel}</li>
     *
     * <li>{@link ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     * <br>The send request was attempted after the account lost
     * {@link Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE} in the {@link TextChannel TextChannel}.</li>
     *
     * <li>{@link ErrorResponse#UNAUTHORIZED UNAUTHORIZED}
     * <br>If this is a {@link PrivateChannel PrivateChannel} and the recipient User blocked you</li>
     *
     * <li>{@link ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     * <br>If this is a {@link PrivateChannel PrivateChannel} and the currently logged in account
     * does not share any Guilds with the recipient User</li>
     *
     * <li>{@link ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     * <br>The send request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param msg
     *         the {@link Message Message} to send
     *
     * @return {@link MessageAction MessageAction}
     * <br>The newly created Message after it has been sent to Discord.
     *
     * @throws InsufficientPermissionException
     *         If this is a {@link TextChannel TextChannel} and the logged in account does not have
     *         <ul>
     *         <li>{@link Permission#MESSAGE_READ Permission.MESSAGE_READ}</li>
     *         <li>{@link Permission#MESSAGE_WRITE Permission.MESSAGE_WRITE}</li>
     *         <li>{@link Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS} (if this message is only
     *         an embed)</li>
     *         </ul>
     * @throws VerificationLevelException
     *         If this is a {@link TextChannel} and {@link TextChannel#getGuild() TextChannel.getGuild()}{@link
     *         Guild#checkVerification() .checkVerification()} returns false.
     * @throws IllegalArgumentException
     *         If the provided message is {@code null} or the provided {@link Message Message} contains an {@link
     *         MessageEmbed MessageEmbed} that is not {@link MessageEmbed#isSendable(AccountType) sendable}
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel PrivateChannel} and both the currently logged in account and the
     *         target user are bots.
     * @see MessageBuilder
     */
    @CheckReturnValue
    public MessageAction sendMessage(Message msg) {return getChannel().sendMessage(msg);}

    public void send(String message) {
        sendMessage(message).queue();
    }

    public void sendFormat(String message, Object... args) {
        sendMessageFormat(message, args).queue();
    }

    public void send(Message message) {
        sendMessage(message).queue();
    }

    /**
     * Checks to see if the bot has the permissions required.
     *
     * @param permission
     *         any permissions required.
     *
     * @return {@code true} if the bot has the permissions required. {@code false} otherwise.
     */
    public boolean checkPermission(Permission... permission) {
        return PermissionUtil.checkPermission(getChannel(), getSelfMember(), permission);
    }

    /**
     * Checks to see if the bot has the permissions required. If the permissions required are not found, the user is
     * notified with a message and this method returns {@code true}. If the required permissions are present, this
     * method returns {@code false}.
     *
     * @param permission
     *         the permissions required.
     *
     * @return {@code false} if the bot has the permissions required. {@code true} otherwise.
     */
    public boolean requirePermission(Permission... permission) {
        return requirePermission(getChannel(), permission);
    }

    /**
     * Checks to see if the bot has the permissions required. If the permissions required are not found, the user is
     * notified with a message and this method returns {@code true}. If the required permissions are present, this
     * method returns {@code false}.
     *
     * @param channel
     *         the channel for which to check permissions on
     * @param permission
     *         the permissions required.
     *
     * @return {@code false} if the bot has the permissions required. {@code true} otherwise.
     */
    public boolean requirePermission(Channel channel, Permission... permission) {
        if (!checkPermission(permission)) {
            reply(MissingPermission.buildResponse(getSelfMember(), channel, getChannel(), permission)).send();
            return true;
        }
        return false;
    }

    public boolean isHelpEvent() {
        return isHelpEvent;
    }

//    public void replyWith(CommandResponse response) {
//        getClient().getResponseManager().acceptResponse(new CommandResponsePacketImpl(this, response, getChannel()));
//    }

    @Override
    public String toString() {
        return String.format("CommandEvent{ Guild=%d, Channel=%d, Author=%d, Prefix=%s, Key=%s, Content=%s }",
                             getGuildId(), getChannelId(), getAuthorId(), getPrefix(), Arrays.toString(getKeys()),
                             getContent());
    }

    // DELEGATED METHODS

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getMentionedUsers} <p> An immutable list of all
     * mentioned {@link net.dv8tion.jda.core.entities.User Users}. <br>If no user was mentioned, this list is empty.
     *
     * @return immutable list of mentioned users
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public List<User> getMentionedUsers() {
        return getMessage().getMentionedUsers();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getMentionedChannels}
     *
     * <p> A immutable list of all mentioned {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}. <br>If
     * none
     * were mentioned, this list is empty.
     *
     * <p><b>This may include TextChannels from other {@link net.dv8tion.jda.core.entities.Guild Guilds}</b>
     *
     * @return immutable list of mentioned TextChannels
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public List<TextChannel> getMentionedChannels() {
        return getMessage().getMentionedChannels();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getMentionedRoles}
     *
     * <p> A immutable list of all mentioned {@link net.dv8tion.jda.core.entities.Role Roles}. <br>If none were
     * mentioned, this list is empty.
     *
     * <p><b>This may include Roles from other {@link net.dv8tion.jda.core.entities.Guild Guilds}</b>
     *
     * @return immutable list of mentioned Roles
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public List<Role> getMentionedRoles() {
        return getMessage().getMentionedRoles();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getMentionedMembers}
     *
     * <p> Creates an immutable list of {@link net.dv8tion.jda.core.entities.Member Members} representing the users of
     * {@link #getMentionedUsers()} in the specified {@link net.dv8tion.jda.core.entities.Guild Guild}. <br>This is only
     * a convenience method and will skip all users that are not in the specified Guild.
     *
     * @param guild
     *         Non-null {@link net.dv8tion.jda.core.entities.Guild Guild} that will be used to retrieve Members.
     *
     * @return Immutable list of mentioned Members
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     * @throws IllegalArgumentException
     *         If the specified Guild is {@code null}
     * @since 3.4.0
     */
    public List<Member> getMentionedMembers(Guild guild) {
        return getMessage().getMentionedMembers(guild);
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getMentionedMembers}
     *
     * <p> Creates an immutable list of {@link net.dv8tion.jda.core.entities.Member Members} representing the users of
     * {@link #getMentionedUsers()} in the {@link net.dv8tion.jda.core.entities.Guild Guild} this Message was sent in.
     * <br>This is only a convenience method and will skip all users that are not in the specified Guild. <br>It will
     * provide the {@link #getGuild()} output Guild to
     * {@link #getMentionedMembers(net.dv8tion.jda.core.entities.Guild)}.
     *
     * @return Immutable list of mentioned Members
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     * @throws IllegalStateException
     *         If this message was not sent in a {@link net.dv8tion.jda.core.entities .TextChannel TextChannel}
     * @since 3.4.0
     */
    public List<Member> getMentionedMembers() {
        return getMessage().getMentionedMembers();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getMentions}
     *
     * <p> Combines all instances of {@link net.dv8tion.jda.core.entities.IMentionable IMentionable} filtered by the
     * specified {@link net.dv8tion.jda.core.entities.Message.MentionType MentionType} values. <br>This does not include
     * {@link #getMentionedMembers()} to avoid duplicates.
     *
     * <p>If no MentionType values are given this will fallback to all types.
     *
     * @param types
     *         Amount of {@link net.dv8tion.jda.core.entities.Message.MentionType MentionTypes} to include in the list
     *         of mentions
     *
     * @return Immutable list of filtered {@link net.dv8tion.jda.core.entities.IMentionable IMentionable} instances
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     * @throws IllegalArgumentException
     *         If provided with {@code null}
     * @since 3.4.0
     */
    public List<IMentionable> getMentions(Message.MentionType... types) {
        return getMessage().getMentions(types);
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#isMentioned}
     *
     * <p> Checks if given {@link net.dv8tion.jda.core.entities.IMentionable IMentionable} was mentioned in this
     * message
     * in any way (@User, @everyone, @here, @Role). <br>If no filtering {@link net.dv8tion.jda.core.entities.Message
     * .MentionType MentionTypes} are specified this will fallback to all mention types.
     *
     * @param mentionable
     *         The mentionable entity to check on.
     * @param types
     *         The types to include when checking whether this type was mentioned. This will be used with {@link
     *         #getMentions(net.dv8tion.jda.core.entities.Message.MentionType...) getMentions(MentionType...)}
     *
     * @return True, if the given mentionable was mentioned in this message
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public boolean isMentioned(IMentionable mentionable, Message.MentionType... types) {
        return getMessage().isMentioned(mentionable, types);
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#mentionsEveryone}
     *
     * <p> Indicates if this Message mentions everyone using @everyone or @here.
     *
     * @return True, if message is mentioning everyone
     */
    public boolean mentionsEveryone() {
        return getMessage().mentionsEveryone();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#isEdited}
     *
     * <p> Returns whether or not this Message has been edited before.
     *
     * @return True if this message has been edited.
     */
    public boolean isEdited() {
        return getMessage().isEdited();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getEditedTime}
     *
     * <p> Provides the {@link java.time.OffsetDateTime OffsetDateTime} defining when this Message was last edited. If
     * this Message has not been edited ({@link #isEdited()} is {@code false}), then this method will return {@code
     * null}.
     *
     * @return Time of the most recent edit, or {@code null} if the Message has never been edited.
     */
    public OffsetDateTime getEditedTime() {
        return getMessage().getEditedTime();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getInvites}
     *
     * <p> Creates an immutable List of {@link net.dv8tion.jda.core.entities.Invite Invite} codes that are included in
     * this Message. <br>This will use the {@link java.util.regex.Pattern Pattern} provided under {@link
     * net.dv8tion.jda.core.entities.Message#INVITE_PATTERN} to construct a {@link java.util.regex.Matcher Matcher} that
     * will parse the {@link net.dv8tion.jda.core.entities.Message#getContentRaw()} output and include all codes it
     * finds in a list.
     *
     * <p>You can use the codes to retrieve/validate invites via {@link net.dv8tion.jda.core.entities.Invite#resolve
     * (net.dv8tion.jda.core.JDA, String) Invite.resolve(JDA, String)}
     *
     * @return Immutable list of invite codes
     */
    public List<String> getInvites() {
        return getMessage().getInvites();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getNonce}
     *
     * <p> Validation <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank" >nonce</a> for this
     * Message <br>This can be used to validate that a Message was properly sent to the Discord Service. <br>To set a
     * nonce before sending you may use {@link net.dv8tion.jda.core.MessageBuilder#setNonce(String)
     * MessageBuilder.setNonce(String)}!
     *
     * @return The validation nonce
     *
     * @see net.dv8tion.jda.core.MessageBuilder#setNonce(String)
     * @see <a href="https://en.wikipedia.org/wiki/Cryptographic_nonce" target="_blank">Cryptographic Nonce -
     * Wikipedia</a>
     * @since 3.4.0
     */
    public String getNonce() {
        return getMessage().getNonce();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#isFromType}
     *
     * <p> Used to determine if this Message was received from a {@link net.dv8tion.jda.core.entities.MessageChannel
     * MessageChannel} of the {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} specified. <br>This will
     * always be false for {@link net.dv8tion.jda.core.entities.ChannelType#VOICE} as Messages can't be sent to {@link
     * net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     *
     * <p>Useful for restricting functionality to a certain type of channels.
     *
     * @param type
     *         The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} to check against.
     *
     * @return True if the {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} which this message was received
     * from is the same as the one specified by {@code type}.
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public boolean isFromType(ChannelType type) {
        return getMessage().isFromType(type);
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getChannelType}
     *
     * <p> Gets the {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} that this message was received from.
     * <br>This will never be {@link net.dv8tion.jda.core.entities.ChannelType#VOICE} as Messages can't be sent to
     * {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     *
     * @return The ChannelType which this message was received from.
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public ChannelType getChannelType() {
        return getMessage().getChannelType();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getPrivateChannel}
     *
     * <p> Returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} that this message was sent
     * in. <br><b>This is only valid if the Message was actually sent in a PrivateChannel.</b> This will return {@code
     * null} if it was not sent from a PrivateChannel. <br>You can check the type of channel this message was sent from
     * using {@link #isFromType(net.dv8tion.jda.core.entities.ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel
     * MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.core.entities.PrivateChannel
     * PrivateChannel}.
     *
     * @return The PrivateChannel this message was sent in, or {@code null} if it was not sent from a PrivateChannel.
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public PrivateChannel getPrivateChannel() {
        return getMessage().getPrivateChannel();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getGroup}
     *
     * <p> Returns the {@link net.dv8tion.jda.client.entities.Group Group} that this message was sent in. <br><b>This
     * is
     * only valid if the Message was actually sent in a Group.</b> This will return {@code null} if it was not sent from
     * a Group. <br>You can check the type of channel this message was sent from using {@link
     * #isFromType(net.dv8tion.jda.core.entities.ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel
     * MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.client.entities.Group Group}.
     *
     * @return The Group this message was sent in, or {@code null} if it was not sent from a Group.
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public Group getGroup() {
        return getMessage().getGroup();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getTextChannel}
     *
     * <p> Returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this message was sent in.
     * <br><b>This is only valid if the Message was actually sent in a TextChannel.</b> This will return {@code null}
     * if
     * it was not sent from a TextChannel. <br>You can check the type of channel this message was sent from using {@link
     * #isFromType(net.dv8tion.jda.core.entities.ChannelType)} or {@link #getChannelType()}.
     *
     * <p>Use {@link #getChannel()} for an ambiguous {@link net.dv8tion.jda.core.entities.MessageChannel
     * MessageChannel}
     * if you do not need functionality specific to {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @return The TextChannel this message was sent in, or {@code null} if it was not sent from a TextChannel.
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public TextChannel getTextChannel() {
        return getMessage().getTextChannel();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getCategory}
     *
     * <p> The {@link net.dv8tion.jda.core.entities.Category Category} this message was sent in. This will always be
     * {@code null} for DMs and Groups. <br>Equivalent to {@code getTextChannel().getParent()}.
     *
     * @return {@link net.dv8tion.jda.core.entities.Category Category} for this message
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core .entities.MessageType#DEFAULT
     *         MessageType.DEFAULT}
     */
    public Category getCategory() {
        return getMessage().getCategory();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getAttachments}
     *
     * <p> An unmodifiable list of {@link net.dv8tion.jda.core.entities.Message.Attachment Attachments} that are
     * attached to this message. <br>Most likely this will only ever be 1 {@link net.dv8tion.jda.core.entities
     * .Message.Attachment Attachment} at most.
     *
     * @return Unmodifiable list of {@link net.dv8tion.jda.core.entities.Message.Attachment Attachments}.
     */
    public List<Message.Attachment> getAttachments() {
        return getMessage().getAttachments();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#isTTS}
     *
     * <p> Defines whether or not this Message triggers TTS (Text-To-Speech).
     *
     * @return If this message is TTS.
     */
    public boolean isTTS() {
        return getMessage().isTTS();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#isPinned}
     *
     * <p> Whether or not this Message has been pinned in its parent channel.
     *
     * @return True - if this message has been pinned.
     */
    public boolean isPinned() {
        return getMessage().isPinned();
    }

    /**
     * Used to add the Message to the {@link #getChannel() MessageChannel's} pinned message list.
     * <br>This is a shortcut method to {@link net.dv8tion.jda.core.entities.MessageChannel#pinMessageById(String)}.
     *
     * <p>The success or failure of this action will not affect the return of {@link #isPinned()}.
     *
     * <p>The following {@link ErrorResponse ErrorResponses} are possible:
     * <ul>
     * <li>{@link ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     * <br>The pin request was attempted after the account lost access to the {@link TextChannel TextChannel}
     * due to {@link Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the account lost access to the
     * {@link Guild Guild} or {@link Group Group} typically due to being kicked or removed.</li>
     *
     * <li>{@link ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     * <br>The pin request was attempted after the account lost
     * {@link Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the {@link TextChannel TextChannel}.</li>
     *
     * <li>{@link ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     * The pin request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @return {@link RestAction RestAction} - Type: {@link Void}
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link MessageType#DEFAULT MessageType.DEFAULT}
     * @throws InsufficientPermissionException
     *         If this Message is from a {@link TextChannel TextChannel} and:
     *         <br><ul>
     *         <li>Missing {@link Permission#MESSAGE_READ Permission.MESSAGE_READ}.
     *         <br>The account needs access the the channel to pin a message in it.</li>
     *         <li>Missing {@link Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}.
     *         <br>Required to actually pin the Message.</li>
     *         </ul>
     */
    @CheckReturnValue
    public RestAction<Void> pin() {
        return getMessage().pin();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#unpin}
     *
     * <p> Used to remove the Message from the {@link net.dv8tion.jda.core.entities.Message#getChannel()
     * MessageChannel's} pinned message list. <br>This is a shortcut method to {@link
     * net.dv8tion.jda.core.entities.MessageChannel#unpinMessageById(String)}.
     *
     * <p>The success or failure of this action will not affect the return of {@link #isPinned()}.
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible: <ul>
     * <li>{@link
     * net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS} <br>The unpin request was attempted
     * after the account lost access to the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} due to {@link
     * net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the account lost access
     * to the {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link net.dv8tion.jda.client.entities.Group Group}
     * typically due to being kicked or removed.</li>
     *
     * <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS
     * MISSING_PERMISSIONS}
     * <br>The unpin request was attempted after the account lost
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the {@link
     * net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *
     * <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE} The unpin request was
     * attempted after the Message had been deleted.</li> </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link Void}
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core.entities .MessageType#DEFAULT
     *         MessageType.DEFAULT}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If this Message is from a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} and: <br><ul>
     *         <li>Missing {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ}. <br>The account
     *         needs access the the channel to pin a message in it.</li> <li>Missing {@link net.dv8tion.jda.core
     *         .Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}. <br>Required to actually pin the Message.</li>
     *         </ul>
     */
    @CheckReturnValue
    public RestAction<Void> unpin() {
        return getMessage().unpin();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#clearReactions}
     *
     * <p> Removes all reactions from this Message. <br>This is useful for moderator commands that wish to remove all
     * reactions at once from a specific message.
     *
     * <p><b>Neither success nor failure of this request will affect this Message's {@link
     * net.dv8tion.jda.core.entities.Message#getReactions()} return as Message is immutable.</b>
     *
     * <p>The following {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} are possible: <ul>
     * <li>{@link
     * net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS} <br>The clear-reactions request was
     * attempted after the account lost access to the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} due
     * to {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} being revoked, or the account
     * lost access to the {@link net.dv8tion.jda.core.entities.Guild Guild} or {@link
     * net.dv8tion.jda.client.entities.Group Group} typically due to being kicked or removed.</li>
     *
     * <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS
     * MISSING_PERMISSIONS}
     * <br>The clear-reactions request was attempted after the account lost
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the {@link
     * net.dv8tion.jda.core.entities.TextChannel TextChannel} when adding the reaction.</li>
     *
     * <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE} The clear-reactions
     * request was attempted after the Message had been deleted.</li> </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link Void}
     *
     * @throws UnsupportedOperationException
     *         If this is not a Received Message from {@link net.dv8tion.jda.core.entities .MessageType#DEFAULT
     *         MessageType.DEFAULT}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the MessageChannel this message was sent in was a {@link net.dv8tion.jda .core.entities.TextChannel
     *         TextChannel} and the currently logged in account does not have {@link net.dv8tion.jda.core
     *         .Permission#MESSAGE_MANAGE Permission .MESSAGE_MANAGE} in the channel.
     * @throws IllegalStateException
     *         If this message was <b>not</b> sent in a {@link net.dv8tion.jda.core.entities .TextChannel TextChannel}.
     */
    @CheckReturnValue
    public RestAction<Void> clearReactions() {
        return getMessage().clearReactions();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.Message#getType}
     *
     * <p> This specifies the {@link net.dv8tion.jda.core.entities.MessageType MessageType} of this Message.
     *
     * <p>Messages can represent more than just simple text sent by Users, they can also be special messages that
     * inform
     * about events occurs. A few examples are the system message informing that a message has been pinned. Another
     * would be the system message informing that a call has been started or ended in a group.
     *
     * @return The {@link net.dv8tion.jda.core.entities.MessageType MessageType} of this message.
     */
    public MessageType getType() {
        return getMessage().getType();
    }

    /**
     * <b>Copied From</b> {@link net.dv8tion.jda.core.entities.ISnowflake#getCreationTime}
     *
     * <p> The time this entity was created. Calculated through the Snowflake in {@link
     * net.dv8tion.jda.core.entities.ISnowflake#getIdLong}.
     *
     * @return OffsetDateTime - Time this entity was created at.
     *
     * @see net.dv8tion.jda.core.utils.MiscUtil#getCreationTime(long)
     */
    public OffsetDateTime getCreationTime() {
        return getMessage().getCreationTime();
    }
}