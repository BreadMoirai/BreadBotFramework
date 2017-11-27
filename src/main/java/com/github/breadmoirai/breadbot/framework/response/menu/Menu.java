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
package com.github.breadmoirai.breadbot.framework.response.menu;

import com.github.breadmoirai.breadbot.framework.response.menu.reactions.MenuReaction;
import com.github.breadmoirai.breadbot.waiter.EventWaiterB;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;
import java.util.function.Predicate;

public abstract class Menu {

    private final Predicate<Member> criteria;

    public Menu(Predicate<Member> criteria) {
        this.criteria = criteria;
    }

    abstract void attachOptions(EmbedBuilder embedBuilder);

    abstract void waitForEvent(MenuResponse responseMenu, EventWaiterB waiter);

    abstract void addReactions(Message message);

    protected boolean checkMember(Member member) {
        return criteria.test(member);
    }

    public abstract List<MenuReaction> getOptions();
}
