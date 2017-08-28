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

import com.github.breadmoirai.bot.framework.event.Arguments;
import com.github.breadmoirai.bot.util.Emoji;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * This represents a space separated argument in a Command. Mentions are eagerly evaluated while everything else is lazily evaluated. Methods begging in {@code parse} do not store the return value and as such, each call parses the content again.
 */
public interface CommandArgument {

    /**
     * This is a getter method.
     *
     * @return The underlying string for this argument.
     */
    String getArgument();

    /**
     * This is a getter method.
     *
     * @return the {@link net.dv8tion.jda.core.JDA} instance.
     */
    JDA getJDA();

    /**
     * This is a getter method.
     *
     * @return The {@link net.dv8tion.jda.core.entities.Guild} this argument was sent in.
     */
    Guild getGuild();

    /**
     * This is a getter method.
     *
     * @return The channel this argument was sent in.
     * @see com.github.breadmoirai.bot.framework.arg.CommandArgument#getTextChannel
     */
    TextChannel getChannel();

    /**
     * If this method returns {@code true}, that means this argument has been eagerly evaluated to a mention. This argument would be of type
     * <ul>
     * <li>User Mention</li>
     * <li>Member Mention</li>
     * <li>Role Mention</li>
     * <li>TextChannel Mention</li>
     * <li>Emote</li>
     * </ul>
     * If this mention returns {@code false} then this argument is not a mention.
     *
     * @return {@code true} if this is a properly formatted mention, {@code false} otherwise.
     */
    default boolean isMention() {
        return false;
    }


//    Checks if this argument is of the type given. If this is a formatted mention, i.e. {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument#isMention isMention()} would return {@code true}, {@code notnull} is also {@code true}, this will also check if the argument can correctly be resolved to a valid {@link net.dv8tion.jda.core.JDA} entity or primitive or custom Object registered with {@link com.github.breadmoirai.bot.framework.event.args.ArgumentTypes}.
//            * <p>If this argument is an {@link net.dv8tion.jda.core.entities.Emote}, the method will return {@code true}. However, if the {@link net.dv8tion.jda.core.entities.Emote} is not a valid {@link net.dv8tion.jda.core.JDA} entity, it will NOT be reflected in this method. Instead the object returned by {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument#getEmote} will return an {@link net.dv8tion.jda.core.entities.Emote} with {@link net.dv8tion.jda.core.entities.IFakeable#isFake() isFake()} returning {@code true}

    /**
     * @see com.github.breadmoirai.bot.framework.arg.ArgumentTypes#isOfType(java.lang.Class, com.github.breadmoirai.bot.framework.arg.CommandArgument)
     *
     * @param type The type of argument to test for.
     *
     * @return {@code true} if this argument is of the type passed.
     */
    default boolean isOfType(Class<?> type) {
        return ArgumentTypes.isOfType(type, this);
    }

    /**
     * @see com.github.breadmoirai.bot.framework.arg.ArgumentTypes#isOfType(java.lang.Class, com.github.breadmoirai.bot.framework.arg.CommandArgument, int)
     *
     * @param type the argument type to test for
     * @param flags testing flags. {@link ArgumentFlags}
     *
     * @return {@code true} if this argument is of the type passed.
     */
    default boolean isOfType(Class<?> type, int flags) {
        return ArgumentTypes.isOfType(type, this, flags);
    }

    default <T> Optional<T> getAsType(Class<T> type) {
        return ArgumentTypes.getAsType(type, this);
    }

    /**
     * Invocation is exactly the same as:
     * <pre><code>
     *     this.{@link CommandArgument#getArgument() getArgument()}.{@link java.lang.String#matches(String) matches}(regex)
     * </code></pre>
     *
     * @param regex the regular expression to which this string is to be matched
     * @return {@code true} if, and only if, this string matches the
     * given regular expression
     */
    default boolean matches(String regex) {
        return getArgument().matches(regex);
    }

    /**
     * Invocation is exactly the same as:
     * <pre><code>
     *     pattern.{@link Pattern#matcher(java.lang.CharSequence) matcher}(this.{@link CommandArgument#getArgument() getArgument()}).{@link java.util.regex.Matcher#matches() matches()}
     * </code></pre>
     *
     * @param pattern the regex Pattern to match the argument with.
     * @return {@code true} if, and only if, this string matches the
     * given regular pattern
     */
    default boolean matches(Pattern pattern) {
        return pattern.matcher(getArgument()).matches();
    }

    /**
     * Checks whether the underlying string consists of only digits with an exception of {@code -} or {@code +} at the beginning.
     * <p>Result is equivalent to a regex of:
     * <pre><code>
     *     [-+]?[0-9]+
     * </code></pre>
     *
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -} or {@code +}
     */
    default boolean isNumeric() {
        return Arguments.isNumber(getArgument());
    }

    /**
     * Equivalent to {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isNumeric isNumeric()} but also checks that the number is within the range of an integer
     *
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -} or {@code +} and the number does not exceed {@link java.lang.Integer#MAX_VALUE} or {@link java.lang.Integer#MIN_VALUE}
     */
    default boolean isInteger() {
        return Arguments.isInteger(getArgument());
    }

    /**
     * Parses the argument as an Integer with {@link java.lang.Integer#parseInt(String)}
     *
     * @return an int
     * @throws NumberFormatException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isInteger isInteger()} returns {@code false}
     */
    default int parseInt() {
        return Integer.parseInt(getArgument());
    }

    /**
     * Equivalent to {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isNumeric isNumeric()} but also checks that the number is within the range of an integer
     *
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -} or {@code +} and the number does not exceed {@link java.lang.Long#MAX_VALUE} or {@link java.lang.Long#MIN_VALUE}
     */
    default boolean isLong() {
        return Arguments.isLong(getArgument());
    }

    /**
     * Parses the argument as an Long with {@link java.lang.Long#parseLong(String)}
     *
     * @return a long
     * @throws NumberFormatException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isInteger isInteger()} returns {@code false}
     */
    default long parseLong() {
        return Long.parseLong(getArgument());
    }

    /**
     * Checks the expression against the regex provided in {@link java.lang.Double#valueOf(String)}
     */
    default boolean isFloat() {
        return Arguments.isFloat(getArgument());
    }

    /**
     * Parses the argument as an Long with {@link java.lang.Long#parseLong(String)}
     *
     * @return a float
     * @throws NumberFormatException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isFloat() isFloat()} returns {@code false}
     */
    default float parseFloat() {
        return Float.parseFloat(getArgument());
    }

    /**
     * Parses the argument as an Long with {@link java.lang.Double#parseDouble(String)}
     *
     * @return a double
     * @throws NumberFormatException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isFloat() isFloat()} returns {@code false}
     */
    default double parseDouble() {
        return Double.parseDouble(getArgument());
    }

    /**
     * A range is defined as two positive integers separated by a dash with no whitespace
     * eg {@code 23-54}
     * <p>
     * Checks whether the underlying string consists of at least 2 digits separated by a dash
     * <p>Result is equivalent to a regex of:
     * <pre><code>
     *     [0-9]+-[0-9]+
     * </code></pre>
     *
     * @return {@code true} if it matches the format required.
     */
    default boolean isRange() {
        return Arguments.isRange(getArgument());
    }

    /**
     * Parses this argument as an inclusive range and returns an {@link java.util.stream.IntStream} consisting of the elements in the stream in the order declared
     * <p>For example, an argument of {@code "2-6"} will return a stream of {@code [2,3,4,5,6]} and an argument of {@code "8-5"} will return a stream of {@code [8,7,6,5]}.
     * <p><b>This method CAN be used in cases where {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isRange isRange()} would return false.</b> If the argument is a single integer, where {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isInteger isInteger()} would return {@code true}, an {@link java.util.stream.IntStream} with a single value will be returned.
     *
     * @return an ordered {@link java.util.stream.IntStream}. If {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isRange isRange()} AND {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isInteger isInteger()} would return false, An empty IntStream will be returned.
     */
    @NotNull
    default IntStream parseRange() {
        return Arguments.parseRange(getArgument());
    }

    /**
     * Checks if this matches a hexadecimal number, specifically whether this argument consists of digits 0-10 and/or letters a-f optionally prefixed by {@code #} or {@code 0x}
     * The result of this method is equivalent to checking this argument against a regex of {@code (#|0x)?[0-9a-fA-F]+}
     *
     * @return {@code true} if it matches the format required.
     */
    default boolean isHex() {
        return Arguments.isHex(getArgument());
    }

    /**
     * Parses hexadecimal into an int
     *
     * @return an int
     * @throws NumberFormatException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isHex isHex()} would return false
     */
    default int parseIntFromHex() {
        String s = getArgument();
        s = Arguments.stripHexPrefix(s);
        return Integer.parseInt(s, 16);
    }

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.User} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@(!)?[0-9]+>}
     *
     * @return {@code true} if this is a formatted {@link net.dv8tion.jda.core.entities.User} mention.
     */
    boolean isUser();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.User} mention that can be correctly resolved to a {@link net.dv8tion.jda.core.entities.User}.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@(!)?[0-9]+>} and then checking to see if {@link net.dv8tion.jda.core.JDA} has knowledge of a {@link net.dv8tion.jda.core.entities.User} with that id.
     *
     * If this method returns {@code false} and {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isUser} returns {@code true}, this CommandArgument is can be cast to an  {@link com.github.breadmoirai.bot.framework.arg.impl.InvalidMentionArgument InvalidMentionArgument}
     *
     * @return {@code true} if this is a formatted {@link net.dv8tion.jda.core.entities.User} mention that can be resolved to an entity.
     */
    boolean isValidUser();

    /**
     * If this is a {@link net.dv8tion.jda.core.entities.User} mention, will return the specified user.
     *
     * @return The {@link net.dv8tion.jda.core.entities.User} if found by JDA.
     *
     * @throws UnsupportedOperationException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isValidUser()} would return {@code false}
     */
    @NotNull
    default User getUser() {
        throw new UnsupportedOperationException("This argument is not a User");
    }

    /**
     * First checks if the argument is a {@link net.dv8tion.jda.core.entities.User} mention.
     * Then attempts to resolve that mention to a {@link net.dv8tion.jda.core.entities.User}.
     * If the {@link net.dv8tion.jda.core.entities.User} is found, the
     * {@link net.dv8tion.jda.core.entities.User} will be checked against the
     * {@link net.dv8tion.jda.core.entities.Guild} to see if it is a
     * {@link net.dv8tion.jda.core.entities.Member}.
     *
     * @return {@code true} if the {@link net.dv8tion.jda.core.entities.Member} can be resolved to a valid JDA entity. Otherwise {@code false}
     */
    boolean isValidMember();

    /**
     * The {@link net.dv8tion.jda.core.entities.Member} if it can be resolved.
     *
     * @return The {@link net.dv8tion.jda.core.entities.Member} if found.
     *
     * @throws UnsupportedOperationException if {@link CommandArgument#isValidMember()} would return {@code false}
     */
    @NotNull
    default Member getMember() {
        throw new UnsupportedOperationException("This argument is not a member");
    }

    /**
     * Searches for a member in the {@link net.dv8tion.jda.core.entities.Guild} using the argument as criteria.
     * If it matches multiple users, the user whose name begins with the argument is given precedence. If multiple users match, the first one found is returned.
     * This attempts to match Username and Nickname.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.Member} mention, that {@link net.dv8tion.jda.core.entities.Member} is returned within the {@link java.util.Optional}.
     *
     * @return the first {@link net.dv8tion.jda.core.entities.Member} found, otherwise an empty {@link java.util.Optional}
     */
    @NotNull
    Optional<Member> findMember();

    /**
     * Searches for members whose Username or Nickname contains this argument.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.Member} mention, a {@link java.util.List} with only that element is returned.
     *
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.Member Members}
     */
    @NotNull
    List<Member> searchMembers();

    /**
     * Checks if this argument is of the same format as a {@link net.dv8tion.jda.core.entities.Role} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@&[0-9]+>}.
     *
     * @return {@code true} if this argument is formatted as a {@link net.dv8tion.jda.core.entities.Role} mention
     */
    boolean isRole();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.Role} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@&[0-9]+>} and checking if it can be correctly resolved to a {@link net.dv8tion.jda.core.entities.Role} within the {@link net.dv8tion.jda.core.entities.Guild}.
     *
     * @return {@code true} if this is a valid {@link net.dv8tion.jda.core.entities.Role} mention that can be resolved to a valid JDA entity.
     */
    boolean isValidRole();

    /**
     * Attempts to resolve this argument to a {@link net.dv8tion.jda.core.entities.Role} in the {@link net.dv8tion.jda.core.entities.Guild}.
     * If {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isRole isRole()} would return true, it is guaranteed that this method returns a {@code non-null} value.
     *
     * @return {@link net.dv8tion.jda.core.entities.Role} if role is present within the {@link net.dv8tion.jda.core.entities.Guild}, otherwise {@code null}
     *
     * @throws UnsupportedOperationException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isValidRole()} would return {@code false}
     */
    @NotNull
    default Role getRole() {
        throw new UnsupportedOperationException("This argument is not a Role");
    }

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.core.entities.Role} by name.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.Role} mention, that {@link net.dv8tion.jda.core.entities.Role} is returned within the {@link java.util.Optional}.
     *
     * @return the first {@link net.dv8tion.jda.core.entities.Role} found, otherwise an empty {@link java.util.Optional}
     */
    @NotNull
    Optional<Role> findRole();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.core.entities.Role Roles} that match this argument. The criteria being that the {@link net.dv8tion.jda.core.entities.Role} name should contain this argument.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.Role} mention, a {@link java.util.List} with only that element is returned.
     *
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.Role Roles}.
     */
    @NotNull
    List<Role> searchRoles();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.TextChannel} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <#[0-9]+>}
     *
     * @return {@code true} if this is a correctly formatted {@link net.dv8tion.jda.core.entities.TextChannel} mention
     */
    boolean isTextChannel();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.TextChannel} mention that can be correctly resolved to a {@link net.dv8tion.jda.core.entities.TextChannel}.
     * The result of this method is equivalent to checking this argument against a regex of {@code <#[0-9]+>} and then checking to see if {@link net.dv8tion.jda.core.JDA} has knowledge of a {@link net.dv8tion.jda.core.entities.TextChannel} with that id.
     *
     * If this method returns {@code false} and {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isTextChannel()} returns {@code true}, this CommandArgument is can be cast to an  {@link com.github.breadmoirai.bot.framework.arg.impl.InvalidMentionArgument InvalidMentionArgument}
     *
     * @return {@code true} if this is a formatted {@link net.dv8tion.jda.core.entities.TextChannel} mention and can be correctly resolved to a JDA entity.
     */
    boolean isValidTextChannel();

    /**
     * Attempts to resolve this argument as a {@link net.dv8tion.jda.core.entities.TextChannel} mention to a {@link net.dv8tion.jda.core.entities.TextChannel} in the {@link net.dv8tion.jda.core.entities.Guild}.
     *
     * <p>If {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isTextChannel isTextChannel()} would return true, it is guaranteed that this method returns a {@code non-null} value.
     *
     * @return {@link net.dv8tion.jda.core.entities.TextChannel} if can be resolved to a JDA entity
     *
     * @throws UnsupportedOperationException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isValidTextChannel()} would return {@code false}
     */
    @NotNull
    default TextChannel getTextChannel() {
        throw new UnsupportedOperationException("This argument is not a TextChannel");
    }

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.core.entities.TextChannel} by name.
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.TextChannel} mention, that {@link net.dv8tion.jda.core.entities.TextChannel} is returned within the {@link java.util.Optional}.
     *
     * @return the first {@link net.dv8tion.jda.core.entities.TextChannel} found, otherwise an empty {@link java.util.Optional}
     */
    @NotNull
    Optional<TextChannel> findTextChannel();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} that match this argument. The criteria being that the {@link net.dv8tion.jda.core.entities.TextChannel} name should contain this argument.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.TextChannel} mention, a {@link java.util.List} with only that element is returned.
     *
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     */
    @NotNull
    List<TextChannel> searchTextChannels();

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.core.entities.VoiceChannel} by name.
     *
     * @return the first {@link net.dv8tion.jda.core.entities.VoiceChannel} if found, otherwise an empty {@link java.util.Optional}
     */
    @NotNull
    Optional<VoiceChannel> findVoiceChannel();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.core.entities.VoiceChannel Roles} that match this argument. The criteria being that the {@link net.dv8tion.jda.core.entities.VoiceChannel} name should contain this argument.
     *
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     */
    @NotNull
    List<VoiceChannel> searchVoiceChannels();

    /**
     * Checks if this argument is in a valid {@link net.dv8tion.jda.core.entities.Emote} mention form.
     * The result of this method is equivalent to checking this argument against a regex of {@code <:.+:[0-9]+>}
     *
     * @return {@code true} if this argument matches the format required
     */
    boolean isEmote();

    /**
     * If {@link #isEmote()} would return {@code true}, this method will always return a {@code not-null} value, {@code null} otherwise.
     * If the formatting is correct but {@link net.dv8tion.jda.core.JDA} cannot resolve the {@link net.dv8tion.jda.core.entities.Emote},
     * a {@link net.dv8tion.jda.core.entities.IFakeable Fake} {@link net.dv8tion.jda.core.entities.Emote} will be returned.
     *
     * <p>If {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isEmote isEmote()} would return true, it is guaranteed that this method returns a {@code non-null} value.
     *
     * @return An {@link net.dv8tion.jda.core.entities.Emote} if the formatting is correct. Otherwise {@code null}.
     *
     * @throws UnsupportedOperationException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isEmote()} would return {@code false}
     */
    @NotNull
    default Emote getEmote() {
        throw new UnsupportedOperationException("This argument is not an Emote");
    }

    /**
     * Will attempt to match this argument against the emojis found in {@link com.github.breadmoirai.bot.util.Emoji} using their unicode value.
     * The implementation of this method is
     * <pre><code>
     *     return {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#getEmoji}() != null
     * </code></pre>
     *
     * @return {@code true} if an {@link com.github.breadmoirai.bot.util.Emoji} was successfully matched.
     */
    boolean isEmoji();

    /**
     * Attempts to find a matching {@link com.github.breadmoirai.bot.util.Emoji} with {@link com.github.breadmoirai.bot.util.Emoji#find(String)}
     *
     * @return The {@link com.github.breadmoirai.bot.util.Emoji} if matched.
     *
     * @throws UnsupportedOperationException if {@link com.github.breadmoirai.bot.framework.arg.CommandArgument#isEmoji()} would return {@code false}
     */
    @NotNull
    default Emoji getEmoji() {
        throw new UnsupportedOperationException("This argument is not an Emoji");
    }
}
