package com.github.breadmoirai.tests;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.internal.event.CommandEventFactoryImpl;
import com.github.breadmoirai.breadbot.framework.internal.event.CommandEventInternal;
import com.github.breadmoirai.breadbot.modules.prefix.DefaultPrefixModule;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import org.jetbrains.annotations.NotNull;

import static org.mockito.Mockito.*;

public class MockFactory {

    @NotNull
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
        when(mockMessage.getRawContent()).thenReturn(input);
        CommandEventInternal event = eventFactory.createEvent(mockEvent, mockMessage, client);

        CommandEventInternal spy = spy(event);

        //when(spy.getChannel()).thenReturn(mockChannel);
        doReturn(0L).when(spy).getChannelId();
        when(spy.getAuthor()).thenReturn(mockUser);
        //doNothing().when(spy).reply(anyString());
        return spy;
    }
}
