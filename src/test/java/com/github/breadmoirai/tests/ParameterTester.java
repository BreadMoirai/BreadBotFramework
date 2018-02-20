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
import com.github.breadmoirai.tests.commands.NameCommand;
import com.github.breadmoirai.tests.commands.ParameterFallbackCommand;
import com.github.breadmoirai.tests.commands.PingCommand;
import com.github.breadmoirai.tests.commands.SSICommand;
import com.github.breadmoirai.tests.commands.WikiParameterCommand;
import org.junit.Test;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static com.github.breadmoirai.tests.MockFactory.mockCommand;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ParameterTester {

    static {
        TestLoggerFactory.getInstance().setPrintLevel(Level.INFO);
    }

    private BreadBot client;

    @Test
    public void width1() {
        client = new BreadBotBuilder()
                .addCommand(SSICommand.class)
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b", "a, b, null");
        assertResponse("!ssi 1 a b", "1, a, null");
        assertResponse("!sis a 1 b", "a, 1, b");
        assertResponse("!sis 1 a b", "1, null, a");
        assertResponse("!sis a b 1", "a, 1, b");
        assertResponse("!iss a", "null, a, null");
    }

    @Test
    public void width0() {
        client = new BreadBotBuilder()
                .addCommand(SSICommand.class, handleBuilder -> handleBuilder.getParameters().forEach(param -> param.setWidth(0)))
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b 1", "a b 1, null, null");
        assertResponse("!iss a b 1", "1, a b, null");
        assertResponse("!iss a b 1 c ddd", "1, a b, c ddd");
    }

    @Test
    public void widthNegative() {
        client = new BreadBotBuilder()
                .addCommand(SSICommand.class, handleBuilder -> handleBuilder.getParameters().forEach(param -> param.setWidth(-1)))
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b 1", "a b 1, null, null");
        assertResponse("!iss a b 1", "null, a b 1, null");
        assertResponse("!iss 1 a b", "null, 1 a b, null");
    }

    @Test
    public void widthPositive() {
        client = new BreadBotBuilder()
                .addCommand(SSICommand.class, handleBuilder -> handleBuilder.getParameters().forEach(param -> param.setWidth(2)))
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b 1", "a b, null, null");
        assertResponse("!iss a b 1 2", "null, a b, 1 2");
        assertResponse("!iss 1 a b 2 4", "null, 1 a, b 2");
    }

    @Test
    public void indexPositive() {
        client = new BreadBotBuilder()
                .addCommand(SSICommand.class, command -> command
                        .configureParameter(0, param -> param.setIndex(3))
                        .configureParameter(1, param -> param.setIndex(2))
                        .configureParameter(2, param -> param.setIndex(1)))
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b 1", "1, b, null");
        assertResponse("!iss a b 1", "1, b, a");
    }

    @Test
    public void indexNegative() {
        client = new BreadBotBuilder()
                .addCommand(SSICommand.class, command -> command
                        .configureParameter(0, param -> param.setIndex(-1))
                        .configureParameter(1, param -> param.setIndex(-2))
                        .configureParameter(2, param -> param.setIndex(-3)))
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b 1", "1, b, null");
        assertResponse("!iss a b 1", "1, b, a");
        assertResponse("!sis a b 1 c", "c, 1, b");
    }

    @Test
    public void parameterPropertyTest() {
        client = new BreadBotBuilder().addCommand(NameCommand.class).build();
        assertResponse("!name a b c", "a b c");
        assertResponse("!first a b c", "a");
        assertResponse("!last a b c", "b c");
    }

    @Test
    public void subParameterPropertyTest() {
        client = new BreadBotBuilder()
                .createCommand(PingCommand.class).addCommand(NameCommand.class)
                .getClientBuilder().build();
        assertResponse("!ping name a b c", "a b c");
        assertResponse("!ping first a b c", "a");
        assertResponse("!ping last a b c", "b c");
    }

    @Test
    public void wikiTest() {
        client = new BreadBotBuilder()
                .addCommand(WikiParameterCommand.class)
                .build();
        assertResponse("!ex hello 1", "lint=1, third=null, start=hello");
        assertResponse("!ex hhel lo 3i wo 6", "lint=6, third=3i, start=hhel lo");
        assertResponse("!ex is i2noi 2i4 sz", "Error: required [lint] but not found");
    }

    @Test
    public void fallbackTest() {
        client = new BreadBotBuilder()
                .addCommand(ParameterFallbackCommand::new)
                .build();
        assertResponse("!fallback to me", "to me");
        assertResponse("!fallback", "default");
    }

    @Test
    public void enclosureTest() {
        client = new BreadBotBuilder()
                .addCommand(SSICommand::new)
                .build();
        assertResponse("!ssi a b 1", "a, b, 1");
        assertResponse("!ssi \"a b\" 1", "a b, 1, null");
        assertResponse("!ssi \"a b 1\"", "a b 1, null, null");
        assertResponse("!ssi \n```java\na b 1\n```", "a b 1, null, null");
    }

    private void assertResponse(final String input, final String expected) {
        CommandEventInternal spy = mockCommand(client, input, MockFactory.UserType.BASIC);


        doAnswer(invocation -> {
            String argument = invocation.getArgument(0);
            assertEquals(expected, argument);
            return null;
        }).when(spy).reply(anyString());

        client.getCommandEngine().handle(spy);

        if (expected != null) {
            verify(spy, times(1)).reply(expected);
        } else {
            verify(spy, never()).reply(anyString());
        }
    }
}
