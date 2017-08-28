package com.github.breadmoirai.bot.framework.arg;

import com.github.breadmoirai.bot.framework.event.Arguments;
import com.github.breadmoirai.bot.framework.arg.impl.*;
import com.github.breadmoirai.bot.util.Emoji;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.jetbrains.annotations.Nullable;

public class CommandArgumentFactory {
    private final JDA jda;
    private final Guild guild;
    private final TextChannel channel;

    public CommandArgumentFactory(JDA jda, Guild guild, TextChannel channel) {
        this.jda = jda;
        this.guild = guild;
        this.channel = channel;
    }

    public CommandArgument parse(final String s) {
        if (Arguments.isMention(s)) {
            int i = 2;
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
                                    return new RoleArgument(jda, guild, channel, s, role);
                                return new InvalidRoleArgument(jda, guild, channel, s, idLong);
                            } else break;
                        case '!':
                            i++;
                        default:
                            String userId = s.substring(i, s.length() - 1);
                            if (Arguments.isLong(userId)) {
                                long idLong = Long.parseLong(userId);
                                Member member = guild.getMemberById(idLong);
                                if (member != null)
                                    return new MemberArgument(jda, guild, channel, s, member);
                                User user = jda.getUserById(idLong);
                                if (user != null)
                                    return new UserArgument(jda, guild, channel, s, user);
                                return new InvalidUserArgument(jda, guild, channel, s, idLong);
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
                            return new TextChannelArgument(jda, guild, channel, s, textChannel);
                        return new InvalidTextChannelArgument(jda, guild, channel, s, idLong);
                    }
                }
                break;
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
                            EmoteImpl emoteImpl = new EmoteImpl(emoteId, (JDAImpl) jda);
                            emoteImpl.setName(name);
                            jdaEmote = emoteImpl;
                        }
                        return new EmoteArgument(jda, guild, channel, s, jdaEmote);
                    }
                }
            }
        }
        CommandArgument x = tryEmoji(s);
        if (x != null) return x;
        return new GenericCommandArgument(jda, guild, channel, s);
    }

    @Nullable
    public CommandArgument tryEmoji(String s) {
        if (s.length() > 11) {
            return null;
        } else if (s.length() > 4) {
            if (s.charAt(0) != '\uD83D') return null;
        } else if (s.length() == 4) {
            if (s.charAt(0) != '\uD83C') return null;
        } else if (s.length() == 2) {
            if (s.charAt(0) < '\uD83C' || s.charAt(0) > '\uD83E')
                return null;
            if (s.charAt(1) != '\u20E3')
                return null;
        } else if (s.length() == 1) {
            char c = s.charAt(0);
            if (c == '\u00A9') return new EmojiArgument(jda, guild, channel, s, Emoji.COPYRIGHT);
            else if (c == '\u00AE') return new EmojiArgument(jda, guild, channel, s, Emoji.REGISTERED);
            else if (c < '\u203C' || c > '\u3299') return null;

        }
        Emoji emoji = Emoji.find(s);
        if (emoji != null) {
            return new EmojiArgument(jda, guild, channel, s, emoji);
        }
        return null;
    }
}
