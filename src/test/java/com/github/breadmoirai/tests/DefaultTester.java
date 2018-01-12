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
import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.tests.commands.AmIAdminCommand;
import com.github.breadmoirai.tests.commands.AuthorCommand;
import com.github.breadmoirai.tests.commands.EchoCommand;
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

    private BreadBotClient client;

    @Test
    public void matchRegexTest() {
        client = new BreadBotClientBuilder().addCommand(EchoCommand::new).build();
        assertResponse("!echo echo", "echo");
        assertResponse("!echonum echo", null);
        assertResponse("!echonum 124", "124");
    }

    @Test
    public void authorTest() {
        client = new BreadBotClientBuilder()
                .addCommand(AuthorCommand::new)
                .build();
        assertResponse(MockFactory.UserType.BASIC, "!author", MockFactory.BASIC_NAME);
        assertResponse(MockFactory.UserType.ADMIN, "!auth", MockFactory.ADMIN_NAME);
        assertResponse(MockFactory.UserType.BASIC, "!auth <@" + MockFactory.SELF_ID + ">", MockFactory.SELF_NAME);
    }

    @Test
    public void adminTest() {
        client = new BreadBotClientBuilder()
                .addDefaultAdminModule()
                .addCommand(AmIAdminCommand::new)
                .build();
        assertResponse(MockFactory.UserType.BASIC, "!isadmin", null);
        assertResponse(MockFactory.UserType.ADMIN, "!isadmin t3ag3", "yes");
        assertResponse(MockFactory.UserType.CREATOR, "!isadmin 32 resg", "yes");
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
