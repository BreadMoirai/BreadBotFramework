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

import com.github.breadmoirai.framework.util.Emoji;
import net.dv8tion.jda.core.entities.*;

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

    default boolean matches(Pattern pattern) {
        return pattern.matcher(getString()).matches();
    }

    /**
     *
     * @return
     */
    default boolean isNumeric() {
        return false;
    }

    int getInt();

    long getLong();

    boolean isFloat();

    float getFloat();

    double getDouble();

    boolean isRange();

    IntStream getRange();

    boolean isHex();

    int getIntFromHex();

    default boolean isUser() {return false;}

    default User getUser() {return null;}

    default boolean isMember() {return false;}

    Member getMemberMention();

    Optional<Member> getAsMember();

    boolean isRole();

    Role getAsRole();

    boolean isTextChannel();

    TextChannel getAsTextChannel();

    boolean isEmoji();

    Emoji getAsEmoji();

    boolean isEmote();

    Emote getAsEmote();

    boolean matchesMember();

    boolean matchesRole();

    boolean matchesTextChannel();

    boolean matchesVoiceChannel();

    VoiceChannel getAsVoiceChannel();
}
