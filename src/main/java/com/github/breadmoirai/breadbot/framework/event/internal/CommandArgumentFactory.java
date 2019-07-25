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

package com.github.breadmoirai.breadbot.framework.event.internal;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.EmojiArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.EmoteArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.GenericCommandArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.InvalidRoleArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.InvalidTextChannelArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.InvalidUserArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.MemberArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.RoleArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.TextChannelArgument;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.UserArgument;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.util.Arguments;
import com.github.breadmoirai.breadbot.util.Emoji;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;

public class CommandArgumentFactory {
    private final CommandEvent event;
    private final JDA jda;
    private final Guild guild;
    private final TextChannel channel;

    public CommandArgumentFactory(CommandEvent event) {
        this.event = event;
        this.jda = event.getJDA();
        this.guild = event.getGuild();
        this.channel = event.getChannel();
    }

    public CommandArgument parse(final String s) {
        if (Arguments.isMention(s)) {
            int i = 2;
            boolean animated = false;
            switch (s.charAt(1)) {
                case '@': {
                    switch (s.charAt(2)) {
                        case '&':
                            i++;
                            String roleId = s.substring(i, s.length() - 1);

                            if (Arguments.isLong(roleId)) {
                                long idLong = Long.parseLong(roleId);
                                Role role = guild.getRoleById(idLong);
                                if (role != null)
                                    return new RoleArgument(event, s, role);
                                return new InvalidRoleArgument(event, s, idLong);
                            } else break;
                        case '!':
                            i++;
                        default:
                            String userId = s.substring(i, s.length() - 1);
                            if (Arguments.isLong(userId)) {
                                long idLong = Long.parseLong(userId);
                                Member member = guild.getMemberById(idLong);
                                if (member != null)
                                    return new MemberArgument(event, s, member);
                                User user = jda.getUserById(idLong);
                                if (user != null)
                                    return new UserArgument(event, s, user);
                                return new InvalidUserArgument(event, s, idLong);
                            }
                    }
                }
                break;
                case '#': {
                    String channelId = s.substring(i, s.length() - 1);
                    if (Arguments.isLong(channelId)) {
                        long idLong = Long.parseLong(channelId);
                        TextChannel textChannel = guild.getTextChannelById(idLong);
                        if (textChannel != null)
                            return new TextChannelArgument(event, s, textChannel);
                        return new InvalidTextChannelArgument(event, s, idLong);
                    }
                }
                break;
                case 'a': {
                    i++;
                    animated = true;
                    //fallthrough
                }
                case ':': {
                    String emoteMention = s.substring(i, s.length() - 1);
                    int seperator = emoteMention.indexOf(':');
                    if (seperator == -1 || seperator == emoteMention.length() - 1) break;
                    String name = emoteMention.substring(0, seperator);
                    String id = emoteMention.substring(seperator + 1, emoteMention.length());
                    if (Arguments.isLong(id) && Arguments.isAlphanumericWithUnderscoresOrDashesWithAMinimumLengthOf2AndAMaximumLengthOf32(name)) {
                        long emoteId = Long.parseLong(id);
                        Emote jdaEmote = jda.getEmoteById(emoteId);
                        if (jdaEmote == null) {
                            //fake emote
                            EmoteImpl emoteImpl = new EmoteImpl(emoteId, (JDAImpl) jda);
                            emoteImpl.setName(name);
                            emoteImpl.setAnimated(animated);
                            jdaEmote = emoteImpl;
                        }
                        return new EmoteArgument(event, s, jdaEmote);
                    }
                }
            }
        }
        CommandArgument x = tryEmoji(s);
        if (x != null) return x;
        return new GenericCommandArgument(event, s);
    }

    private CommandArgument tryEmoji(String s) {
        if (s.length() > 11) {
            return null;
        } else if (s.length() > 4) {
            if (s.charAt(0) != '\uD83D') return null;
        } else if (s.length() == 4) {
            if (s.charAt(0) != '\uD83C') return null;
        } else if (s.length() == 2) {
            if (s.charAt(0) < '\uD83C' || s.charAt(0) > '\uD83E')
                if (s.charAt(1) != '\u20E3')
                    return null;
        } else if (s.length() == 1) {
            char c = s.charAt(0);
            if (c == '\u00A9') return new EmojiArgument(event, s, Emoji.COPYRIGHT);
            else if (c == '\u00AE') return new EmojiArgument(event, s, Emoji.REGISTERED);
            else if (c < '\u203C' || c > '\u3299') return null;

        }
        Emoji emoji = Emoji.find(s);
        if (emoji != null) {
            return new EmojiArgument(event, s, emoji);
        }
        return null;
    }
}
