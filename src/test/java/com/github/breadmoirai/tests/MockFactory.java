/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.tests;

import com.github.breadmoirai.breadbot.framework.BreadBot;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventFactoryImpl;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.breadbot.plugins.prefix.UnmodifiablePrefixPlugin;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MockFactory {

    public static final long CHANNEL_ID = 14L;
    public static final long USER_ID = 9L;
    public static final long SELF_ID = 1L;
    public static final String BASIC_NAME = "bob";
    public static final String ADMIN_NAME = "andy";
    public static final String CREATOR_NAME = "cindy";
    public static final String SELF_NAME = "shamaya";
    private static Member selfMember;
    private static User selfUser;

    static {
        selfMember = mock(Member.class);
        selfUser = mock(User.class);
        when(selfMember.getUser()).thenReturn(selfUser);
        when(selfUser.getIdLong()).thenReturn(SELF_ID);
        when(selfUser.getName()).thenReturn(SELF_NAME);
        when(selfMember.getEffectiveName()).thenReturn(SELF_NAME);
    }

    public static CommandEventInternal mockCommand(BreadBot client, String input, UserType userType) {
        //        GenericGuildMessageEvent mockInput = mock(GenericGuildMessageEvent.class);
//        Guild mockGuild = mock(Guild.class);
//        when(mockGuild.getIdLong()).thenReturn(0L);
//        when(mockInput.getGuild()).thenReturn(mockGuild);
//

//        final String[] split = DiscordPatterns.WHITE_SPACE.split(input.substring(1), 2);
//        final String key = split[0];
//        final String content = split.length > 1 ? split[1].trim() : null;

        CommandEventFactoryImpl eventFactory = new CommandEventFactoryImpl(new UnmodifiablePrefixPlugin("!"));

        GenericGuildMessageEvent mockEvent = mock(GenericGuildMessageEvent.class);

        Guild mockGuild = mock(Guild.class);
        when(mockEvent.getGuild()).thenReturn(mockGuild);
        when(mockGuild.getSelfMember()).thenReturn(selfMember);
        when(mockGuild.getMemberById(SELF_ID)).thenReturn(selfMember);
        when(mockGuild.getMember(selfUser)).thenReturn(selfMember);

        Member mockMember = getMockMember(userType);
        User mockUser = mockMember.getUser();

        when(mockGuild.getMember(mockUser)).thenReturn(mockMember);
        when(mockMember.getGuild()).thenReturn(mockGuild);

        Message mockMessage = mock(Message.class);
        when(mockMessage.getContentRaw()).thenReturn(input);
        when(mockMessage.getAuthor()).thenReturn(mockUser);

        CommandEventInternal event = eventFactory.createEvent(mockEvent, mockMessage, client);
        CommandEventInternal spy = spy(event);
        doAnswer(o -> CHANNEL_ID).when(spy).getChannelId();

        when(mockGuild.getMemberById(USER_ID)).thenReturn(mockMember);

        //doNothing().when(spy).reply(anyString());
        return spy;
    }

    private static Member getMockMember(UserType userType) {
        final Member mock = mock(Member.class);
        final User mocku = mock(User.class);
        when(mock.getUser()).thenReturn(mocku);
        when(mocku.getIdLong()).thenReturn(USER_ID);


        switch (userType) {
            case BASIC:
                when(mocku.getName()).thenReturn(BASIC_NAME);
                when(mock.getEffectiveName()).thenReturn(BASIC_NAME);

                when(mock.hasPermission(any(Permission.class))).thenReturn(false);
                when(mock.canInteract(any(Member.class))).thenReturn(false);
                break;
            case ADMIN:
                when(mocku.getName()).thenReturn(ADMIN_NAME);
                when(mock.getEffectiveName()).thenReturn(ADMIN_NAME);

                when(mock.hasPermission(Permission.KICK_MEMBERS)).thenReturn(true);
                when(mock.canInteract(selfMember)).thenReturn(true);
                break;
            case CREATOR:
                when(mocku.getName()).thenReturn(CREATOR_NAME);
                when(mock.getEffectiveName()).thenReturn(CREATOR_NAME);

                when(mock.hasPermission(Permission.KICK_MEMBERS)).thenReturn(true);
                when(mock.canInteract(any(Member.class))).thenReturn(true);
                break;
        }
        return mock;
    }

    public enum UserType {BASIC, ADMIN, CREATOR;}


}
