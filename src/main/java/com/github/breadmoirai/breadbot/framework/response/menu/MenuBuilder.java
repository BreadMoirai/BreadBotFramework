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

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.Checks;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;

public abstract class MenuBuilder {

    private Predicate<Member> criteria;

    /**
     * Sets a list of users whose reactions are accepted on this menu.
     * <p>Note that this will override any existing criteria
     *
     * @param users the users to accept
     */
    public MenuBuilder setAcceptedUsers(User... users) {
        long[] longs = Arrays.stream(users).mapToLong(ISnowflake::getIdLong).sorted().toArray();
        return setCriteria(member -> {
            long idLong = member.getUser().getIdLong();
            return Arrays.binarySearch(longs, idLong) >= 0;
        });
    }

    /**
     * Sets this menu's criteria to accept reactions from a member who belongs to any of the passed roles.
     * <p>Note that this will override any existing criteria
     *
     * @param roles the roles to accept
     * @return
     */
    public MenuBuilder setAcceptedRoles(Role... roles) {

        long[] longs = Arrays.stream(roles).mapToLong(ISnowflake::getIdLong).sorted().toArray();
        return setCriteria(member -> {
            LongStream roleIdStream = member.getRoles().stream().mapToLong(ISnowflake::getIdLong);
            return roleIdStream.anyMatch(value -> Arrays.binarySearch(longs, value) >= 0);
        });
    }

    public MenuBuilder setCriteria(Predicate<Member> criteria) {
        this.criteria = criteria;
        return this;
    }

    public MenuBuilder addCriteria(Predicate<Member> criteria, boolean and) {
        Checks.notNull(criteria, "criteria");
        if (and) {
            this.criteria = this.criteria.and(criteria);
        } else {
            this.criteria = this.criteria.or(criteria);
        }
        return this;
    }


    public MenuResponse buildResponse(Consumer<EmbedBuilder> embedCustomizer) {
        final EmbedBuilder eb = new EmbedBuilder();
        embedCustomizer.accept(eb);
        return buildResponse(eb);
    }

    public MenuResponse buildResponse(EmbedBuilder embed) {
        final Menu menu = build();
        menu.attachOptions(embed);
        return new MenuResponse(menu, new MessageBuilder().setEmbed(embed.build()).build(), false);
    }

    public MenuResponse buildResponse(Message message) {
        final Menu menu = build();
        return new MenuResponse(menu, message, false);
    }

    public MenuResponse attachTo(Message message) {
        if (message.getChannel() == null) {
            throw new UnsupportedOperationException("This menu can not be attached to Messages created from a MessageBuilder.");
        }
        return new MenuResponse(build(), message, true);
    }

    protected abstract Menu build();

    public MenuResponse buildResponse(String message) {
        Menu menu = build();
        menu.getOptions()

    }
}
