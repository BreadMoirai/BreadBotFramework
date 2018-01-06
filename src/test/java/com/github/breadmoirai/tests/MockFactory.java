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

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventFactoryImpl;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.breadbot.modules.prefix.DefaultPrefixModule;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MockFactory {

    public static CommandEventInternal mockCommand(BreadBotClient client, String input) {
        //        GenericGuildMessageEvent mockInput = mock(GenericGuildMessageEvent.class);
//        Guild mockGuild = mock(Guild.class);
//        when(mockGuild.getIdLong()).thenReturn(0L);
//        when(mockInput.getGuild()).thenReturn(mockGuild);
//

//        final String[] split = DiscordPatterns.WHITE_SPACE.split(input.substring(1), 2);
//        final String key = split[0];
//        final String content = split.length > 1 ? split[1].trim() : null;

        CommandEventFactoryImpl eventFactory = new CommandEventFactoryImpl(new DefaultPrefixModule("!"));

        GenericGuildMessageEvent mockEvent = mock(GenericGuildMessageEvent.class);

        Guild mockGuild = mock(Guild.class);
        when(mockEvent.getGuild()).thenReturn(mockGuild);
        User mockUser = mock(User.class);
//        TextChannel mockChannel = mock(TextChannel.class);
        Message mockMessage = mock(Message.class);
        when(mockMessage.getContentRaw()).thenReturn(input);
        CommandEventInternal event = eventFactory.createEvent(mockEvent, mockMessage, client);

        CommandEventInternal spy = spy(event);

        //when(spy.getChannel()).thenReturn(mockChannel);
        doReturn(0L).when(spy).getChannelId();
        when(spy.getAuthor()).thenReturn(mockUser);
        //doNothing().when(spy).reply(anyString());
        return spy;
    }
}
