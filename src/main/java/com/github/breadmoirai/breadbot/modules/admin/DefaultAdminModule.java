/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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
package com.github.breadmoirai.breadbot.modules.admin;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

import java.util.function.Predicate;

public class DefaultAdminModule implements AdminModule {

    private Predicate<Member> adminPredicate;

    public DefaultAdminModule(Predicate<Member> adminPredicate) {
        this.adminPredicate = adminPredicate;
    }

    public DefaultAdminModule() {
        this(member -> member.canInteract(member.getGuild().getSelfMember()) && member.hasPermission(Permission.KICK_MEMBERS));
    }

    @Override
    public boolean isAdmin(Member member) {
        return adminPredicate.test(member);
    }
}
