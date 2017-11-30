/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.internal.parameter.arguments;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenericCommandArgument implements CommandArgument {

    private final CommandEvent event;

    private final String arg;

    public GenericCommandArgument(CommandEvent event, String arg) {
        this.event = event;
        this.arg = arg;
    }

    @Override
    public CommandEvent getEvent() {
        return event;
    }

    @Override
    public String getArgument() {
        return arg;
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
        List<Member> members = getEvent().getGuild().getMembers();
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
        List<Role> roles = getEvent().getGuild().getRoles();
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
        List<TextChannel> channels = getEvent().getGuild().getTextChannels();
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
        List<VoiceChannel> channels = getEvent().getGuild().getVoiceChannels();
        Stream<VoiceChannel> exact = channels.stream().filter(channel -> channel.getName().equalsIgnoreCase(arg));
        Stream<VoiceChannel> contains = channels.stream().filter(channel -> channel.getName().toLowerCase().contains(arg));
        return Stream.concat(exact, contains);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericCommandArgument that = (GenericCommandArgument) o;

        if (event != null ? !event.equals(that.event) : that.event != null) return false;
        return arg != null ? arg.equals(that.arg) : that.arg == null;
    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + (arg != null ? arg.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getArgument();
    }
}
