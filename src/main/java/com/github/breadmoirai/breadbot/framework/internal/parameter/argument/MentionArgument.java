package com.github.breadmoirai.breadbot.framework.internal.parameter.argument;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.internal.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.util.Emoji;
import net.dv8tion.jda.core.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class MentionArgument implements CommandArgument {

    private final CommandEvent event;
    private final String arg;

    public MentionArgument(CommandEvent event, String arg) {
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
    public boolean isMention() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public int parseInt() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isLong() {
        return false;
    }

    @Override
    public long parseLong() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isFloat() {
        return false;
    }

    @Override
    public float parseFloat() {
        throw new NumberFormatException(arg);
    }

    @Override
    public double parseDouble() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isRange() {
        return false;
    }

    @NotNull
    @Override
    public IntStream parseRange() {
        return IntStream.empty();
    }

    @Override
    public boolean isHex() {
        return false;
    }

    @Override
    public int parseIntFromHex() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean parseBoolean() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isUser() {
        return false;
    }

    @Override
    public boolean isValidUser() {
        return false;
    }

    @NotNull
    @Override
    public User getUser() {
        return null;
    }

    @Override
    public boolean isValidMember() {
        return false;
    }

    @NotNull
    @Override
    public Member getMember() {
        return null;
    }

    @NotNull
    @Override
    public Optional<Member> findMember() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public List<Member> searchMembers() {
        return Collections.emptyList();
    }

    @Override
    public boolean isRole() {
        return false;
    }

    @Override
    public boolean isValidRole() {
        return false;
    }

    @NotNull
    @Override
    public Role getRole() {
        return null;
    }

    @NotNull
    @Override
    public Optional<Role> findRole() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public List<Role> searchRoles() {
        return Collections.emptyList();
    }

    @Override
    public boolean isTextChannel() {
        return false;
    }

    @Override
    public boolean isValidTextChannel() {
        return false;
    }

    @NotNull
    @Override
    public TextChannel getTextChannel() {
        return null;
    }

    @NotNull
    @Override
    public Optional<TextChannel> findTextChannel() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public List<TextChannel> searchTextChannels() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Optional<VoiceChannel> findVoiceChannel() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public List<VoiceChannel> searchVoiceChannels() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEmote() {
        return false;
    }

    @NotNull
    @Override
    public Emote getEmote() {
        return null;
    }

    @Override
    public boolean isEmoji() {
        return false;
    }

    @NotNull
    @Override
    public Emoji getEmoji() {
        return null;
    }

    @Override
    public String toString() {
        return getArgument();
    }
}
