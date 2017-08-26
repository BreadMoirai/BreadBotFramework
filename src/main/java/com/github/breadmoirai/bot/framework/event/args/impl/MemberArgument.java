package com.github.breadmoirai.bot.framework.event.args.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MemberArgument extends UserArgument {

    private final Member member;

    public MemberArgument(JDA jda, Guild guild, TextChannel channel, String arg, Member member) {
        super(jda, guild, channel, arg, member.getUser());
        this.member = member;
    }

    @Override
    public boolean isValidMember() {
        return true;
    }

    @NotNull
    @Override
    public Member getMember() {
        return member;
    }

    @NotNull
    @Override
    public Optional<Member> findMember() {
        return Optional.of(member);
    }

    @NotNull
    @Override
    public List<Member> searchMembers() {
        return Collections.singletonList(member);
    }
}
