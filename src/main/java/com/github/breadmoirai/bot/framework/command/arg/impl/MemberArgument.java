package com.github.breadmoirai.bot.framework.command.arg.impl;

import com.github.breadmoirai.bot.framework.event.CommandEvent;
import net.dv8tion.jda.core.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MemberArgument extends UserArgument {

    private final Member member;

    public MemberArgument(CommandEvent event, String s, Member member) {
        super(event, s, member.getUser());
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
