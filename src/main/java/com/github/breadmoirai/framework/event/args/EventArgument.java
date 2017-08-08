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

import java.util.regex.Pattern;
import java.util.stream.IntStream;

public interface EventArgument {

    String getString();

    boolean matches(String regex);

    boolean matches(Pattern pattern);

    boolean isNumeric();

    int getAsInt();

    long getAsLong();

    boolean isFloat();

    float getAsFloat();

    double getAsDouble();

    boolean isRange();

    IntStream getRange();

    boolean isHex();

    int getAsIntFromHex();

    boolean isUser();

    User getAsUser();

    boolean isMember();

    Member getAsMember();

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
