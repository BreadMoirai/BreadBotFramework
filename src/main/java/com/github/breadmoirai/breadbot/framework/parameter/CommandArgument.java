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

package com.github.breadmoirai.breadbot.framework.parameter;

import com.github.breadmoirai.breadbot.framework.BreadBot;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.util.Arguments;
import com.github.breadmoirai.breadbot.util.Emoji;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * This represents a space separated argument in a Command. Mentions are eagerly evaluated while everything else is
 * lazily evaluated. Methods begging in {@code parse} do not store the return value and as such, each call parses the
 * content again.
 */
public interface CommandArgument {

    /**
     * Gets the CommandEvent for this argument.
     *
     * @return a {@link CommandEvent}
     */
    CommandEvent getEvent();

    /**
     * Returns the backing string of this argument in it's original form.
     *
     * @return The underlying string for this argument.
     */
    String getArgument();

    /**
     * Returns the command client
     *
     * @return a BreadBotClient
     */
    default BreadBot getClient() {
        return getEvent().getClient();
    }

    /**
     * If this method returns {@code true}, that means this argument has been eagerly evaluated to a mention. This
     * argument would be of type
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

    /**
     * Grabs the corresponding parser from the BreadBotClient and attempts to parse this argument to the passed type. If
     * successful, return the result.
     *
     * @param type the class of the type
     * @param <T> The type
     * @return the result if can be parsed, otherwise {@code null}.
     */
    default <T> T getAsType(Class<T> type) {
        TypeParser<T> parser = getClient().getArgumentTypes().getTypeParser(type);
        if (parser != null)
            return parser.parse(this);
        else
            return null;
    }

    /**
     * Invocation is exactly the same as:
     * <pre><code>
     *     this.{@link CommandArgument#getArgument() getArgument()}.{@link java.lang.String#matches(String)
     * matches}(regex)
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
     *     this.{@link CommandArgument#getArgument() getArgument()}.{@link java.lang.String#matches(String)
     * matches}(regex)
     * </code></pre>
     *
     * @param regex the regular expression to which this string is to be matched
     * @param flags any match flags. See {@link Pattern#compile(String, int)}
     * @return {@code true} if, and only if, this string matches the given regular expression
     * @see Pattern#compile(String, int)
     */
    default boolean matches(String regex, int flags) {
        return Pattern.compile(regex, flags).matcher(getArgument()).matches();
    }

    /**
     * Invocation is exactly the same as:
     * <pre><code>
     *     pattern.{@link Pattern#matcher(java.lang.CharSequence) matcher}(this.{@link CommandArgument#getArgument()
     * getArgument()}).{@link java.util.regex.Matcher#matches() matches()}
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
     * Checks whether the underlying string consists of only digits with an exception of {@code -} or {@code +} at the
     * beginning.
     * <p>Result is equivalent to a regex of:
     * <pre><code>
     *     [-+]?[0-9]+
     * </code></pre>
     *
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -}
     * or {@code +}
     */
    default boolean isNumeric() {
        return Arguments.isNumber(getArgument());
    }

    /**
     * Equivalent to {@link CommandArgument#isNumeric isNumeric()} but also checks that the number is within the range
     * of an integer
     *
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -}
     * or {@code +} and the number does not exceed {@link java.lang.Integer#MAX_VALUE} or {@link
     * java.lang.Integer#MIN_VALUE}
     */
    default boolean isInteger() {
        return Arguments.isInteger(getArgument());
    }

    /**
     * Parses the argument as an Integer with {@link java.lang.Integer#parseInt(String)}
     *
     * @return an int
     * @throws NumberFormatException if {@link CommandArgument#isInteger isInteger()} returns {@code false}
     */
    default int parseInt() {
        return Integer.parseInt(getArgument());
    }

    /**
     * Equivalent to {@link CommandArgument#isNumeric isNumeric()} but also checks that the number is within the range
     * of an integer
     *
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -}
     * or {@code +} and the number does not exceed {@link java.lang.Long#MAX_VALUE} or {@link java.lang.Long#MIN_VALUE}
     */
    default boolean isLong() {
        return Arguments.isLong(getArgument());
    }

    /**
     * Parses the argument as an Long with {@link java.lang.Long#parseLong(String)}
     *
     * @return a long
     * @throws NumberFormatException if {@link CommandArgument#isInteger isInteger()} returns {@code false}
     */
    default long parseLong() {
        return Long.parseLong(getArgument());
    }

    /**
     * Checks the expression against the regex provided in {@link java.lang.Double#valueOf(String)}
     *
     * @return {@code true} if this argument can be parsed to a Float or Double
     * @see Double#parseDouble(String)
     */
    default boolean isFloat() {
        return Arguments.isFloat(getArgument());
    }

    /**
     * Parses the argument as an Long with {@link java.lang.Long#parseLong(String)}
     *
     * @return a float
     * @throws NumberFormatException if {@link CommandArgument#isFloat() isFloat()} returns {@code false}
     */
    default float parseFloat() {
        return Float.parseFloat(getArgument());
    }

    /**
     * Parses the argument as an Long with {@link java.lang.Double#parseDouble(String)}
     *
     * @return a double
     * @throws NumberFormatException if {@link CommandArgument#isFloat() isFloat()} returns {@code false}
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
     * Parses this argument as an inclusive range and returns an {@link java.util.stream.IntStream} consisting of the
     * elements in the stream in the order declared
     * <p>For example, an argument of {@code "2-6"} will return a stream of {@code [2,3,4,5,6]} and an argument of
     * {@code "8-5"} will return a stream of {@code [8,7,6,5]}.
     * <p><b>This method CAN be used in cases where {@link CommandArgument#isRange isRange()} would return false.</b> If
     * the argument is a single integer, where {@link CommandArgument#isInteger isInteger()} would return {@code true},
     * an {@link java.util.stream.IntStream} with a single value will be returned.
     *
     * @return an ordered {@link java.util.stream.IntStream}. If {@link CommandArgument#isRange isRange()} AND {@link
     * CommandArgument#isInteger isInteger()} would return false, {@code null} will be returned.
     */
    default IntStream parseRange() {
        return Arguments.parseRange(getArgument());
    }

    /**
     * Checks if this matches a hexadecimal number, specifically whether this argument consists of digits 0-10 and/or
     * letters a-f optionally prefixed by {@code #} or {@code 0x}
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
     * @throws NumberFormatException if {@link CommandArgument#isHex isHex()} would return false
     */
    default int parseIntFromHex() {
        String s = getArgument();
        s = Arguments.stripHexPrefix(s);
        return Integer.parseInt(s, 16);
    }

    /**
     * Please refer to {@link Arguments#isBoolean}
     *
     * @return {@code true} if this can be parsed to a boolean. {@code false} otherwise.
     * @see Arguments#isBoolean(String)
     */
    default boolean isBoolean() {
        return Arguments.isBoolean(getArgument());
    }

    default boolean parseBoolean() {
        return Arguments.parseBoolean(getArgument());
    }

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.api.entities.User} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@(!)?[0-9]+>}
     *
     * @return {@code true} if this is a formatted {@link net.dv8tion.jda.api.entities.User} mention.
     */
    boolean isUser();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.api.entities.User} mention that can be correctly resolved to
     * a {@link net.dv8tion.jda.api.entities.User}.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@(!)?[0-9]+>} and
     * then checking to see if {@link net.dv8tion.jda.api.JDA} has knowledge of a {@link
     * net.dv8tion.jda.api.entities.User} with that id.
     * <p>
     * If this method returns {@code false} and {@link CommandArgument#isUser} returns {@code true}, this
     * CommandArgument is can be cast to an
     * {@link com.github.breadmoirai.breadbot.framework.event.internal.arguments.InvalidMentionArgument
     * InvalidMentionArgument}
     *
     * @return {@code true} if this is a formatted {@link net.dv8tion.jda.api.entities.User} mention that can be
     * resolved to an entity.
     */
    boolean isValidUser();

    /**
     * If this is a {@link net.dv8tion.jda.api.entities.User} mention, will return the specified user.
     *
     * @return The {@link net.dv8tion.jda.api.entities.User} if found by JDA.
     * @throws UnsupportedOperationException if {@link CommandArgument#isValidUser()} would return {@code false}
     */

    default User getUser() {
        throw new UnsupportedOperationException("\"" + this.getArgument() + "\" is not a User");
    }

    /**
     * First checks if the argument is a {@link net.dv8tion.jda.api.entities.User} mention.
     * Then attempts to resolve that mention to a {@link net.dv8tion.jda.api.entities.User}.
     * If the {@link net.dv8tion.jda.api.entities.User} is found, the
     * {@link net.dv8tion.jda.api.entities.User} will be checked against the
     * {@link net.dv8tion.jda.api.entities.Guild} to see if it is a
     * {@link net.dv8tion.jda.api.entities.Member}.
     *
     * @return {@code true} if the {@link net.dv8tion.jda.api.entities.Member} can be resolved to a valid JDA entity.
     * Otherwise {@code false}
     */
    boolean isValidMember();

    /**
     * The {@link net.dv8tion.jda.api.entities.Member} if it can be resolved.
     *
     * @return The {@link net.dv8tion.jda.api.entities.Member} if found.
     * @throws UnsupportedOperationException if {@link CommandArgument#isValidMember()} would return {@code false}
     */

    default Member getMember() {
        throw new UnsupportedOperationException("\"" + this.getArgument() + "\" is not a member");
    }

    /**
     * Searches for a member in the {@link net.dv8tion.jda.api.entities.Guild} using the argument as criteria.
     * If it matches multiple users, the user whose name begins with the argument is given precedence. If multiple users
     * match, the first one found is returned.
     * This attempts to match Username and Nickname.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.api.entities.Member} mention, that {@link
     * net.dv8tion.jda.api.entities.Member} is returned within the {@link java.util.Optional}.
     *
     * @return the first {@link net.dv8tion.jda.api.entities.Member} found, otherwise an empty
     * {@link java.util.Optional}
     */

    Optional<Member> findMember();

    /**
     * Searches for members whose Username or Nickname contains this argument.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.api.entities.Member} mention, a {@link
     * java.util.List} with only that element is returned.
     *
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.api.entities.Member Members}
     */

    List<Member> searchMembers();

    /**
     * Checks if this argument is of the same format as a {@link net.dv8tion.jda.api.entities.Role} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@&[0-9]+>}.
     *
     * @return {@code true} if this argument is formatted as a {@link net.dv8tion.jda.api.entities.Role} mention
     */
    boolean isRole();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.api.entities.Role} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@&[0-9]+>} and
     * checking if it can be correctly resolved to a {@link net.dv8tion.jda.api.entities.Role} within the {@link
     * net.dv8tion.jda.api.entities.Guild}.
     *
     * @return {@code true} if this is a valid {@link net.dv8tion.jda.api.entities.Role} mention that can be resolved
     * to a valid JDA entity.
     */
    boolean isValidRole();

    /**
     * Attempts to resolve this argument to a {@link net.dv8tion.jda.api.entities.Role} in the {@link
     * net.dv8tion.jda.api.entities.Guild}.
     * If {@link CommandArgument#isRole isRole()} would return true, it is guaranteed that this method returns a {@code
     * non-null} value.
     *
     * @return {@link net.dv8tion.jda.api.entities.Role} if role is present within the {@link
     * net.dv8tion.jda.api.entities.Guild}, otherwise {@code null}
     * @throws UnsupportedOperationException if {@link CommandArgument#isValidRole()} would return {@code false}
     */
    default Role getRole() {
        throw new UnsupportedOperationException("\"" + this.getArgument() + "\" is not a Role");
    }

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.api.entities.Role} by name.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.api.entities.Role} mention, that {@link
     * net.dv8tion.jda.api.entities.Role} is returned within the {@link java.util.Optional}.
     *
     * @return the first {@link net.dv8tion.jda.api.entities.Role} found, otherwise an empty {@link java.util.Optional}
     */
    Optional<Role> findRole();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.api.entities.Role Roles} that match this argument.
     * The criteria being that the {@link net.dv8tion.jda.api.entities.Role} name should contain this argument.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.api.entities.Role} mention, a {@link
     * java.util.List} with only that element is returned.
     *
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.api.entities.Role Roles}.
     */
    List<Role> searchRoles();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.api.entities.TextChannel} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <#[0-9]+>}
     *
     * @return {@code true} if this is a correctly formatted {@link net.dv8tion.jda.api.entities.TextChannel} mention
     */
    boolean isTextChannel();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.api.entities.TextChannel} mention that can be correctly
     * resolved to a {@link net.dv8tion.jda.api.entities.TextChannel}.
     * The result of this method is equivalent to checking this argument against a regex of {@code <#[0-9]+>} and then
     * checking to see if {@link net.dv8tion.jda.api.JDA} has knowledge of a {@link
     * net.dv8tion.jda.api.entities.TextChannel} with that id.
     * <p>
     * If this method returns {@code false} and {@link CommandArgument#isTextChannel()} returns {@code true}, this
     * CommandArgument is can be cast to an
     * {@link com.github.breadmoirai.breadbot.framework.event.internal.arguments.InvalidMentionArgument
     * InvalidMentionArgument}
     *
     * @return {@code true} if this is a formatted {@link net.dv8tion.jda.api.entities.TextChannel} mention and can be
     * correctly resolved to a JDA entity.
     */
    boolean isValidTextChannel();

    /**
     * Attempts to resolve this argument as a {@link net.dv8tion.jda.api.entities.TextChannel} mention to a {@link
     * net.dv8tion.jda.api.entities.TextChannel} in the {@link net.dv8tion.jda.api.entities.Guild}.
     *
     * <p>If {@link CommandArgument#isTextChannel isTextChannel()} would return true, it is guaranteed that this method
     * returns a {@code non-null} value.
     *
     * @return {@link net.dv8tion.jda.api.entities.TextChannel} if can be resolved to a JDA entity
     * @throws UnsupportedOperationException if {@link CommandArgument#isValidTextChannel()} would return {@code false}
     */
    default TextChannel getTextChannel() {
        throw new UnsupportedOperationException("\"" + this.getArgument() + "\" is not a TextChannel");
    }

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.api.entities.TextChannel} by name.
     * <p>If this argument is already a valid {@link net.dv8tion.jda.api.entities.TextChannel} mention, that {@link
     * net.dv8tion.jda.api.entities.TextChannel} is returned within the {@link java.util.Optional}.
     *
     * @return the first {@link net.dv8tion.jda.api.entities.TextChannel} found, otherwise an empty
     * {@link java.util.Optional}
     */
    Optional<TextChannel> findTextChannel();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} that match
     * this argument. The criteria being that the {@link net.dv8tion.jda.api.entities.TextChannel} name should contain
     * this argument.
     *
     * <p>If this argument is already a valid {@link net.dv8tion.jda.api.entities.TextChannel} mention, a {@link
     * java.util.List} with only that element is returned.
     *
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.api.entities.TextChannel TextChannels}.
     */
    List<TextChannel> searchTextChannels();

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.api.entities.VoiceChannel} by name.
     *
     * @return the first {@link net.dv8tion.jda.api.entities.VoiceChannel} if found, otherwise an empty {@link
     * java.util.Optional}
     */
    Optional<VoiceChannel> findVoiceChannel();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.api.entities.VoiceChannel Roles} that match this
     * argument. The criteria being that the {@link net.dv8tion.jda.api.entities.VoiceChannel} name should contain this
     * argument.
     *
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}.
     */
    List<VoiceChannel> searchVoiceChannels();

    /**
     * Checks if this argument is in a valid {@link net.dv8tion.jda.api.entities.Emote} mention form.
     * The result of this method is equivalent to checking this argument against a regex of {@code <:.+:[0-9]+>}
     *
     * @return {@code true} if this argument matches the format required
     */
    boolean isEmote();

    /**
     * If {@link #isEmote()} would return {@code true}, this method will always return a {@code not-null} value, {@code
     * null} otherwise.
     * If the formatting is correct but {@link net.dv8tion.jda.api.JDA} cannot resolve the {@link
     * net.dv8tion.jda.api.entities.Emote},
     * a {@link net.dv8tion.jda.api.entities.IFakeable Fake} {@link net.dv8tion.jda.api.entities.Emote} will be
     * returned.
     *
     * <p>If {@link CommandArgument#isEmote isEmote()} would return true, it is guaranteed that this method returns a
     * {@code non-null} value.
     *
     * @return An {@link net.dv8tion.jda.api.entities.Emote} if the formatting is correct. Otherwise {@code null}.
     * @throws UnsupportedOperationException if {@link CommandArgument#isEmote()} would return {@code false}
     */
    default Emote getEmote() {
        throw new UnsupportedOperationException("\"" + this.getArgument() + "\" is not an Emote");
    }

    /**
     * Will attempt to match this argument against the emojis found in
     * {@link com.github.breadmoirai.breadbot.util.Emoji}
     * using their unicode value.
     * The implementation of this method is
     * <pre><code>
     *     return {@link CommandArgument#getEmoji}() != null
     * </code></pre>
     *
     * @return {@code true} if an {@link com.github.breadmoirai.breadbot.util.Emoji} was successfully matched.
     */
    boolean isEmoji();

    /**
     * Attempts to find a matching {@link com.github.breadmoirai.breadbot.util.Emoji} with {@link
     * com.github.breadmoirai.breadbot.util.Emoji#find(String)}
     *
     * @return The {@link com.github.breadmoirai.breadbot.util.Emoji} if matched.
     * @throws UnsupportedOperationException if {@link CommandArgument#isEmoji()} would return {@code false}
     */
    default Emoji getEmoji() {
        throw new UnsupportedOperationException("\"" + this.getArgument() + "\" is not an Emoji");
    }
}