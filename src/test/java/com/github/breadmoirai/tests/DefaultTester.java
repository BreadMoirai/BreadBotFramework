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
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.tests.commands.AuthorCommand;
import com.github.breadmoirai.tests.commands.EchoCommand;
import com.github.breadmoirai.tests.commands.TimeCommand;
import com.github.breadmoirai.tests.commands.WhoAmICommand;
import org.junit.Test;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static com.github.breadmoirai.tests.MockFactory.mockCommand;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class DefaultTester {


    static {
        TestLoggerFactory.getInstance().setPrintLevel(Level.DEBUG);
    }

    private BreadBot client;

    @Test
    public void matchRegexTest() {
        client = new BreadBotBuilder().addCommand(EchoCommand::new).build();
        assertResponse("!echo echo", "echo");
        assertResponse("!echonum echo", null);
        assertResponse("!echonum 124", "124");
    }

    @Test
    public void authorTest() {
        client = new BreadBotBuilder()
                .addCommand(AuthorCommand::new)
                .build();
        assertResponse(MockFactory.UserType.BASIC, "!author", MockFactory.BASIC_NAME);
        assertResponse(MockFactory.UserType.ADMIN, "!auth", MockFactory.ADMIN_NAME);
        assertResponse(MockFactory.UserType.BASIC, "!auth <@" + MockFactory.SELF_ID + ">", MockFactory.SELF_NAME);
    }

    @Test
    public void adminTest() {
        client = new BreadBotBuilder()
                .addAdminPlugin()
                .addCommand(WhoAmICommand::new)
                .build();
        assertResponse(MockFactory.UserType.BASIC, "!isadmin", null);
        assertResponse(MockFactory.UserType.ADMIN, "!isadmin", "yes");
        assertResponse(MockFactory.UserType.CREATOR, "!isadmin", "yes");
    }

    @Test
    public void timeTest() {
        client = new BreadBotBuilder()
                .addCommand(TimeCommand::new)
                .addCommand(TimeCommand::new, command -> command.setKeys("d2").getParameter(0).setWidth(2))
                .build();
        assertResponse("!d 1m", "PT1M");
        assertResponse("!d 1m 1h 1s", "PT1H1M1S");
        assertResponse("!d 1day", "PT24H");
        assertResponse("!d2 1 day 5 hours 10 minutes 13 seconds", "PT29H10M13S");
        assertResponse("!d awff2 70s 21ttg1 4h 1raf", "PT4H1M10S");
        assertResponse("!d2 hello 2 sec my name is 4 h", "PT4H2S");
    }

    private void assertResponse(String input, String expected) {
        assertResponse(MockFactory.UserType.BASIC, input, expected);
    }

    private void assertResponse(final MockFactory.UserType userType, final String input, final String expected) {
        CommandEventInternal spy = mockCommand(client, input, userType);


        doAnswer(invocation -> {
            String argument = invocation.getArgument(0);
            assertEquals(expected, argument);
            return null;
        }).when(spy).reply(anyString());


        client.getCommandEngine().handle(spy);

        if (expected != null)
            verify(spy).reply(expected);
        else
            verify(spy, never()).reply(anyString());
    }


}
