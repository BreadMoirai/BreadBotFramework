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
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public interface CommandArgument {

    String getString();

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
    boolean isInteger();

    /**
     * Parses the argument as an Integer with {@link java.lang.Integer#parseInt(String)}
     * @return an int
     * @throws NumberFormatException if {@link com.github.breadmoirai.framework.event.args.CommandArgument#isInteger isInteger()} returns {@code false}
     */
    int getInt();

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
    long getLong();

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
    float getFloat();

    /**
     * Parses the argument as an Long with {@link java.lang.Double#parseDouble(String)}
     *
     * @return a double
     * @throws NumberFormatException if {@link com.github.breadmoirai.framework.event.args.CommandArgument#isFloat() isFloat()} returns {@code false}
     */
    double getDouble();

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
    IntStream getRange();

    /**
     * Checks if this matches a hexadecimal number
     *
     * Checks whether this argument consists of digits 0-10 and letters a-f optional prefixed by {@code #} or {@code 0x}
     * <p>Result is equivalent to a regex of:
     * <pre><code>
     *     (#|0x)?[0-9a-fA-F]+
     * </code></pre>
     *
     * @return {@code true} if it matches the format required.
     */
    boolean isHex();

    /**
     * Parses hexadecimal into an int
     * @return an int
     * @throws NumberFormatException if {@link com.github.breadmoirai.framework.event.args.CommandArgument#isHex isHex()} would return false
     */
    int getIntFromHex();

    /**
     * Checks if this argument is a {@link net.dv8tion.jda.core.entities.User} mention.
     * <p>Result is equivalent to a regex of:
     * <pre><code>
     *     <(!)?[0-9]+>
     * </code></pre>
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
    Member getMember();

    /**
     * Searches for a member in the guild using the argument as criteria.
     * If it matches multiple users, the user whose name begins with the argument is given precedence. If multiple users match, the first one found is returned.
     * This attempts to match Username and Nickname.
     * @return an <code>{@link java.util.Optional}<{@link net.dv8tion.jda.core.entities.Member}></code>
     */
    Optional<Member> findMember();

    /**
     * Searches for members whose Username or Nickname contains this argument.
     * @return A never-null {@link java.util.List} of {@link net.dv8tion.jda.core.entities.Member Members}
     */
    List<Member> searchMembers();

    boolean isRole();

    Role getRole();

    Optional<Role> findRole();

    List<Role> searchRoles();

    boolean isTextChannel();

    TextChannel getTextChannel();

    Optional<TextChannel> findTextChannel();

    List<TextChannel> searchTextChannels();

    Optional<VoiceChannel> findVoiceChannel();

    List<VoiceChannel> searchVoiceChannels();

    boolean isEmoji();

    Emoji getEmoji();

    boolean isEmote();

    Emote getEmote();
}
