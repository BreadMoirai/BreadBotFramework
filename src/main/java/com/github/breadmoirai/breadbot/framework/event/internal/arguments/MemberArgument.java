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

package com.github.breadmoirai.breadbot.framework.event.internal.arguments;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import net.dv8tion.jda.api.entities.Member;

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


    @Override
    public Member getMember() {
        return member;
    }


    @Override
    public Optional<Member> findMember() {
        return Optional.of(member);
    }


    @Override
    public List<Member> searchMembers() {
        return Collections.singletonList(member);
    }
}
