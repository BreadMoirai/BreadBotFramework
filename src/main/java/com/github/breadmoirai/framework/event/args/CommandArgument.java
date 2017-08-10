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
package com.github.breadmoirai.framework.event.args;

import com.github.breadmoirai.framework.event.Arguments;
import com.github.breadmoirai.framework.util.Emoji;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * This represents a space separated argument in a Command.
 */
public interface CommandArgument {

    /**
     * This is a getter method.
     * @return The underlying string for this argument.
     */
    String getString();

    /**
     * This is a getter method.
     * Returns the {@link net.dv8tion.jda.core.JDA} instance.
     */
    JDA getJDA();

    /**
     * This is a getter method.
     * @return The {@link net.dv8tion.jda.core.entities.Guild} this argument was sent in.
     */
    Guild getGuild();

    /**
     * This is a getter method.
     * @return The channel this argument was sent in.
     * @see com.github.breadmoirai.framework.event.args.CommandArgument#getTextChannel
     */
    TextChannel getChannel();

    /**
     * Invocation is exactly the same as:
     * <pre><code>
     *     this.{@link CommandArgument#getString() getString()}.{@link java.lang.String#matches(String) matches}(regex)
     * </code></pre>
     *
     * @param   regex
     *          the regular expression to which this string is to be matched
     *
     * @return  {@code true} if, and only if, this string matches the
     *          given regular expression
     */
    default boolean matches(String regex) {
        return getString().matches(regex);
    }
    /**
     * Invocation is exactly the same as:
     * <pre><code>
     *     pattern.{@link Pattern#matcher(java.lang.CharSequence) matcher}(this.{@link CommandArgument#getString() getString()}).{@link java.util.regex.Matcher#matches() matches()}
     * </code></pre>
     *
     * @param   pattern
     *          the regex Pattern to match the argument with.
     *
     * @return  {@code true} if, and only if, this string matches the
     *          given regular pattern
     */
    default boolean matches(Pattern pattern) {
        return pattern.matcher(getString()).matches();
    }

    /**
     * Checks whether the underlying string consists of only digits with an exception of {@code -} or {@code +} at the beginning.
     * <p>Result is equivalent to a regex of:
     * <pre><code>
     *     [-+]?[0-9]+
     * </code></pre>
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -} or {@code +}
     */
    default boolean isNumeric() {
        return Arguments.isNumber(getString());
    }

    /**
     * Equivalent to {@link com.github.breadmoirai.framework.event.args.CommandArgument#isNumeric isNumeric()} but also checks that the number is within the range of an integer
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -} or {@code +} and the number does not exceed {@link java.lang.Integer#MAX_VALUE} or {@link java.lang.Integer#MIN_VALUE}
     */
    default boolean isInteger() {
        return Arguments.isInteger(getString());
    }

    /**
     * Parses the argument as an Integer with {@link java.lang.Integer#parseInt(String)}
     * @return an int
     * @throws NumberFormatException if {@link com.github.breadmoirai.framework.event.args.CommandArgument#isInteger isInteger()} returns {@code false}
     */
    default int parseInt() {
        return Integer.parseInt(getString());
    }

    /**
     * Equivalent to {@link com.github.breadmoirai.framework.event.args.CommandArgument#isNumeric isNumeric()} but also checks that the number is within the range of an integer
     * @return {@code true} if and only if this argument only contains {@code 0-9} with an optional prefix of {@code -} or {@code +} and the number does not exceed {@link java.lang.Long#MAX_VALUE} or {@link java.lang.Long#MIN_VALUE}
     */
    boolean isLong();

    /**
     * Parses the argument as an Long with {@link java.lang.Long#parseLong(String)}
     *
     * @return a long
     * @throws NumberFormatException if {@link com.github.breadmoirai.framework.event.args.CommandArgument#isInteger isInteger()} returns {@code false}
     */
    long parseLong();

    /**
     * Checks the expression against the regex provided in {@link java.lang.Double#valueOf(String)}
     */
    boolean isFloat();

    /**
     * Parses the argument as an Long with {@link java.lang.Long#parseLong(String)}
     *
     * @return a float
     * @throws NumberFormatException if {@link com.github.breadmoirai.framework.event.args.CommandArgument#isFloat() isFloat()} returns {@code false}
     */
    float parseFloat();

    /**
     * Parses the argument as an Long with {@link java.lang.Double#parseDouble(String)}
     *
     * @return a double
     * @throws NumberFormatException if {@link com.github.breadmoirai.framework.event.args.CommandArgument#isFloat() isFloat()} returns {@code false}
     */
    double parseDouble();

    /**
     * A range is defined as two positive integers separated by a dash with no whitespace
     * eg {@code 23-54}
     *
     * Checks whether the underlying string consists of at least 2 digits separated by a dash
     * <p>Result is equivalent to a regex of:
     * <pre><code>
     *     [0-9]+-[0-9]+
     * </code></pre>
     *
     * @return {@code true} if it matches the format required.
     */
    boolean isRange();

    /**
     * Parses this argument as an inclusive range and returns an {@link java.util.stream.IntStream} consisting of the elements in the stream in the order declared
     * <p>For example, an argument of {@code "2-6"} will return a stream of {@code [2,3,4,5,6]} and an argument of {@code "8-5"} will return a stream of {@code [8,7,6,5]}
     *
     * @return an ordered {@link java.util.stream.IntStream}. If {@link com.github.breadmoirai.framework.event.args.CommandArgument#isRange isRange()} would return false, An empty IntStream will be returned.
     */
    @NotNull
    IntStream parseRange();

    /**
     * Checks if this matches a hexadecimal number, specifically whether this argument consists of digits 0-10 and/or letters a-f optionally prefixed by {@code #} or {@code 0x}
     * The result of this method is equivalent to checking this argument against a regex of {@code (#|0x)?[0-9a-fA-F]+}
     *
     * @return {@code true} if it matches the format required.
     */
    boolean isHex();

    /**
     * Parses hexadecimal into an int
     * @return an int
     * @throws NumberFormatException if {@link com.github.breadmoirai.framework.event.args.CommandArgument#isHex isHex()} would return false
     */
    int parseIntFromHex();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.User} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@(!)?[0-9]+>}
     *
     * @return {@code true} if this is a formatted {@link net.dv8tion.jda.core.entities.User} mention.
     */
    default boolean isUser() {return false;}

    /**
     * If this is a {@link net.dv8tion.jda.core.entities.User} mention, will return the specified user.
     * @return The {@link net.dv8tion.jda.core.entities.User} if found by JDA. Otherwise, {@code null}.
     */
    default User getUser() {return null;}

    /**
     * First checks if the argument is a {@link net.dv8tion.jda.core.entities.User} mention.
     * Then attempts to resolve that mention to a {@link net.dv8tion.jda.core.entities.User}.
     * If the {@link net.dv8tion.jda.core.entities.User} is found, the
     * {@link net.dv8tion.jda.core.entities.User} will be checked against the
     * {@link net.dv8tion.jda.core.entities.Guild} to see if it is a
     * {@link net.dv8tion.jda.core.entities.Member}.
     * @return The {@link net.dv8tion.jda.core.entities.Member} if it can be resolved. Otherwise {@code null}
     */
    default boolean isMember() {return false;}

    /**
     * The {@link net.dv8tion.jda.core.entities.Member} if it can be resolved.
     * @return The {@link net.dv8tion.jda.core.entities.Member} if found, otherwise {@code null}
     */
    @Nullable
    Member getMember();

    /**
     * Searches for a member in the {@link net.dv8tion.jda.core.entities.Guild} using the argument as criteria.
     * If it matches multiple users, the user whose name begins with the argument is given precedence. If multiple users match, the first one found is returned.
     * This attempts to match Username and Nickname.
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.Member} mention, that {@link net.dv8tion.jda.core.entities.Member} is returned within the {@link java.util.Optional}.
     * @return the first {@link net.dv8tion.jda.core.entities.Member} found, otherwise an empty {@link java.util.Optional}
     */
    @NotNull
    Optional<Member> findMember();

    /**
     * Searches for members whose Username or Nickname contains this argument.
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.Member} mention, a {@link java.util.List} with only that element is returned.
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.Member Members}
     */
    @NotNull
    List<Member> searchMembers();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.Role} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <@&[0-9]+>}
     * @return {@code true} if this is a correctly formatted {@link net.dv8tion.jda.core.entities.Role} mention
     */
    boolean isRole();

    /**
     * Attempts to resolve this argument to a {@link net.dv8tion.jda.core.entities.Role} in the {@link net.dv8tion.jda.core.entities.Guild}.
     * @return {@link net.dv8tion.jda.core.entities.Role} if role is present within the {@link net.dv8tion.jda.core.entities.Guild}, otherwise {@code null}
     */
    @Nullable
    Role getRole();

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.core.entities.Role} by name.
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.Role} mention, that {@link net.dv8tion.jda.core.entities.Role} is returned within the {@link java.util.Optional}.
     * @return the first {@link net.dv8tion.jda.core.entities.Role} found, otherwise an empty {@link java.util.Optional}
     */
    @NotNull
    Optional<Role> findRole();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.core.entities.Role Roles} that match this argument. The criteria being that the {@link net.dv8tion.jda.core.entities.Role} name should contain this argument.
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.Role} mention, a {@link java.util.List} with only that element is returned.
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.Role Roles}.
     */
    @NotNull
    List<Role> searchRoles();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.TextChannel} mention.
     * The result of this method is equivalent to checking this argument against a regex of {@code <#[0-9]+>}
     * @return {@code true} if this is a correctly formatted {@link net.dv8tion.jda.core.entities.TextChannel} mention
     */
    boolean isTextChannel();

    /**
     * Attempts to resolve this argument as a {@link net.dv8tion.jda.core.entities.TextChannel} mention to a {@link net.dv8tion.jda.core.entities.TextChannel} in the {@link net.dv8tion.jda.core.entities.Guild}.
     * @return {@link net.dv8tion.jda.core.entities.Role} role is present within the {@link net.dv8tion.jda.core.entities.Guild}, otherwise {@code null}
     */
    @Nullable
    TextChannel getTextChannel();

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.core.entities.TextChannel} by name.
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.TextChannel} mention, that {@link net.dv8tion.jda.core.entities.TextChannel} is returned within the {@link java.util.Optional}.
     * @return the first {@link net.dv8tion.jda.core.entities.TextChannel} found, otherwise an empty {@link java.util.Optional}
     */
    @NotNull
    Optional<TextChannel> findTextChannel();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} that match this argument. The criteria being that the {@link net.dv8tion.jda.core.entities.TextChannel} name should contain this argument.
     * <p>If this argument is already a valid {@link net.dv8tion.jda.core.entities.TextChannel} mention, a {@link java.util.List} with only that element is returned.
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     */
    @NotNull
    List<TextChannel> searchTextChannels();

    /**
     * Attempts to match this argument to a {@link net.dv8tion.jda.core.entities.VoiceChannel} by name.
     * @return the first {@link net.dv8tion.jda.core.entities.VoiceChannel} if found, otherwise an empty {@link java.util.Optional}
     */
    @NotNull
    Optional<VoiceChannel> findVoiceChannel();

    /**
     * Returns a {@link java.util.List} of {@link net.dv8tion.jda.core.entities.VoiceChannel Roles} that match this argument. The criteria being that the {@link net.dv8tion.jda.core.entities.VoiceChannel} name should contain this argument.
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     */
    @NotNull
    List<VoiceChannel> searchVoiceChannels();

    /**
     * Will attempt to match this argument against the emojis found in {@link com.github.breadmoirai.framework.util.Emoji} using their unicode value.
     * @return {@code true} if an {@link com.github.breadmoirai.framework.util.Emoji} was successfully matched.
     */
    boolean isEmoji();

    /**
     * Attempts to find a matching {@link com.github.breadmoirai.framework.util.Emoji}.
     * @return The {@link com.github.breadmoirai.framework.util.Emoji} if matched, null otherwise.
     */
    @Nullable
    Emoji getEmoji();

    /**
     * Checks if this argument is in a valid {@link net.dv8tion.jda.core.entities.Emote} mention form.
     * The result of this method is equivalent to checking this argument against a regex of {@code <:.+:[0-9]+>}
     * @return {@code true} if this argument matches the format required
     */
    boolean isEmote();

    /**
     * If {@link #isEmote()} would return {@code true}, this method will always return a {@code not-null} value, {@code null} otherwise.
     * If the formatting is correct but {@link net.dv8tion.jda.core.JDA} cannot resolve the {@link net.dv8tion.jda.core.entities.Emote},
     * a {@link net.dv8tion.jda.core.entities.IFakeable Fake} {@link net.dv8tion.jda.core.entities.Emote} will be returned.
     * @return An {@link net.dv8tion.jda.core.entities.Emote} if the formatting is correct. Otherwise {@code null}.
     */
    @Nullable
    Emote getEmote();
}
