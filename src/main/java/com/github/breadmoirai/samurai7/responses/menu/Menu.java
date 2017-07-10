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
package com.github.breadmoirai.samurai7.responses.menu;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import com.github.breadmoirai.samurai7.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;

public abstract class Menu {

    private TLongList acceptedRoles;
    private TLongList acceptedUsers;

    private transient Message message;

    protected void setAcceptedRoles(Role... roles) {
        if (acceptedRoles == null) acceptedRoles = new TLongArrayList();
        acceptedRoles.addAll(Arrays.stream(roles).mapToLong(Role::getIdLong).toArray());
    }

    protected void setAcceptedRoles(long... roles) {
        if (acceptedRoles == null) acceptedRoles = new TLongArrayList();
        acceptedRoles.addAll(roles);
    }

    protected void setAcceptedUsers(User... users) {
        if (acceptedUsers == null) acceptedUsers = new TLongArrayList();
        acceptedUsers.addAll(Arrays.stream(users).mapToLong(User::getIdLong).toArray());
    }

    protected void setAcceptedUsers(long... users) {
        if (acceptedUsers == null) acceptedUsers = new TLongArrayList();
        acceptedUsers.addAll(users);
    }

    abstract void attachOptions(EmbedBuilder embedBuilder);

    abstract void waitForEvent(ResponseMenu responseMenu, EventWaiter waiter);

    abstract void addReactions(Message message);

    abstract void onDelete(ResponseMenu menu);

    protected boolean checkMember(Member member) {
        return (acceptedRoles == null || member.getRoles().stream().mapToLong(Role::getIdLong).anyMatch(acceptedRoles::contains))
                && (acceptedUsers == null || acceptedUsers.contains(member.getUser().getIdLong()));
    }

    Message getMessage() {
        return message;
    }

    void setMessage(Message message) {
        this.message = message;
    }
}
