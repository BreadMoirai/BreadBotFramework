package com.github.breadmoirai.bot.framework.event.args.impl;

import com.github.breadmoirai.bot.framework.event.args.CommandArgument;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenericCommandArgument implements CommandArgument {

    private final JDA jda;
    private final Guild guild;
    private final TextChannel channel;

    private final String arg;

    public GenericCommandArgument(JDA jda, Guild guild, TextChannel channel, String arg) {
        this.jda = jda;
        this.guild = guild;
        this.channel = channel;
        this.arg = arg;
    }

    @Override
    public String getArgument() {
        return arg;
    }

    @Override
    public JDA getJDA() {
        return jda;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public TextChannel getChannel() {
        return channel;
    }

    @Override
    public boolean isUser() {
        return false;
    }

    @Override
    public boolean isValidUser() {
        return false;
    }

    @Override
    public boolean isValidMember() {
        return false;
    }

    @NotNull
    @Override
    public Optional<Member> findMember() {
        return memberStream().findFirst();
    }

    @NotNull
    @Override
    public List<Member> searchMembers() {
        return memberStream().distinct().collect(Collectors.toList());
    }

    @Override
    public boolean isRole() {
        return false;
    }

    @Override
    public boolean isValidRole() {
        return false;
    }

    private Stream<Member> memberStream() {
        final String arg = this.getArgument().toLowerCase();
        List<Member> members = getGuild().getMembers();
        Stream<Member> startsWith = members.stream().filter(member -> member.getEffectiveName().toLowerCase().startsWith(arg));
        Stream<Member> contains = members.stream().filter(member -> member.getEffectiveName().toLowerCase().contains(arg));
        Stream<Member> userContains = members.stream().filter(member -> member.getNickname() != null && member.getUser().getName().toLowerCase().contains(arg));
        return Stream.concat(Stream.concat(startsWith, contains), userContains);
    }


    @NotNull
    @Override
    public Optional<Role> findRole() {
        return roleStream().findFirst();
    }

    @NotNull
    @Override
    public List<Role> searchRoles() {
        return roleStream().distinct().collect(Collectors.toList());
    }

    @Override
    public boolean isTextChannel() {
        return false;
    }

    @Override
    public boolean isValidTextChannel() {
        return false;
    }


    private Stream<Role> roleStream() {
        final String arg = getArgument().toLowerCase();
        List<Role> roles = getGuild().getRoles();
        Stream<Role> exact = roles.stream().filter(role -> role.getName().equalsIgnoreCase(arg));
        Stream<Role> contains = roles.stream().filter(role -> role.getName().toLowerCase().contains(arg));
        return Stream.concat(exact, contains);
    }

    @NotNull
    @Override
    public Optional<TextChannel> findTextChannel() {
        return textChannelStream().findFirst();
    }

    @NotNull
    @Override
    public List<TextChannel> searchTextChannels() {
        return textChannelStream().distinct().collect(Collectors.toList());
    }

    private Stream<TextChannel> textChannelStream() {
        final String arg = getArgument().toLowerCase();
        List<TextChannel> channels = getGuild().getTextChannels();
        Stream<TextChannel> exact = channels.stream().filter(channel -> channel.getName().equalsIgnoreCase(arg));
        Stream<TextChannel> contains = channels.stream().filter(channel -> channel.getName().toLowerCase().contains(arg));
        return Stream.concat(exact, contains);
    }

    @NotNull
    @Override
    public Optional<VoiceChannel> findVoiceChannel() {
        return voiceChannelStream().findFirst();
    }

    @NotNull
    @Override
    public List<VoiceChannel> searchVoiceChannels() {
        return voiceChannelStream().distinct().collect(Collectors.toList());
    }

    @Override
    public boolean isEmote() {
        return false;
    }

    @Override
    public boolean isEmoji() {
        return false;
    }

    private Stream<VoiceChannel> voiceChannelStream() {
        final String arg = getArgument().toLowerCase();
        List<VoiceChannel> channels = getGuild().getVoiceChannels();
        Stream<VoiceChannel> exact = channels.stream().filter(channel -> channel.getName().equalsIgnoreCase(arg));
        Stream<VoiceChannel> contains = channels.stream().filter(channel -> channel.getName().toLowerCase().contains(arg));
        return Stream.concat(exact, contains);
    }

    @Override
    public int hashCode() {
        int result = channel != null ? channel.hashCode() : 0;
        result = 31 * result + arg.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericCommandArgument that = (GenericCommandArgument) o;

        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;
        return arg.equals(that.arg);
    }

    @Override
    public String toString() {
        return getArgument();
    }
}
