package com.github.breadmoirai.tests;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.internal.event.CommandEventInternal;
import com.github.breadmoirai.tests.commands.NameCommand;
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
import static org.mockito.Mockito.verify;

public class ParameterTester {

    static {
        TestLoggerFactory.getInstance().setPrintLevel(Level.INFO);
    }

    private BreadBotClient client;

    @Test
    public void width1() {
        client = new BreadBotClientBuilder()
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
        client = new BreadBotClientBuilder()
                .addCommand(SSICommand.class, handleBuilder -> handleBuilder.getParameters().forEach(param -> param.setWidth(0)))
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b 1", "a b 1, null, null");
        assertResponse("!iss a b 1", "1, a b, null");
        assertResponse("!iss a b 1 c \"ddd\"", "1, a b, c ddd");
    }

    @Test
    public void widthNegative() {
        client = new BreadBotClientBuilder()
                .addCommand(SSICommand.class, handleBuilder -> handleBuilder.getParameters().forEach(param -> param.setWidth(-1)))
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b 1", "a b 1, null, null");
        assertResponse("!iss a b 1", "null, a b 1, null");
        assertResponse("!iss 1 a b", "null, 1 a b, null");
    }

    @Test
    public void widthPositive() {
        client = new BreadBotClientBuilder()
                .addCommand(SSICommand.class, handleBuilder -> handleBuilder.getParameters().forEach(param -> param.setWidth(2)))
                .build();
        assertResponse("!ssi", "null, null, null");
        assertResponse("!ssi a b 1", "a b, null, null");
        assertResponse("!iss a b 1 2", "null, a b, 1 2");
        assertResponse("!iss 1 a b 2 4", "null, 1 a, b 2");
    }

    @Test
    public void indexPositive() {
        client = new BreadBotClientBuilder()
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
        client = new BreadBotClientBuilder()
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
        client = new BreadBotClientBuilder().addCommand(NameCommand.class).build();
        assertResponse("!name a b c", "a b c");
        assertResponse("!first a b c", "a");
        assertResponse("!last a b c", "b c");
    }

    @Test
    public void subParameterPropertyTest() {
        client = new BreadBotClientBuilder()
                .createCommand(PingCommand.class).addCommand(NameCommand.class)
                .getClientBuilder().build();
        assertResponse("!ping name a b c", "a b c");
        assertResponse("!ping first a b c", "a");
        assertResponse("!ping last a b c", "b c");
    }

    @Test
    public void wikiTest() {
        client = new BreadBotClientBuilder()
                .addCommand(WikiParameterCommand.class)
                .build();
        assertResponse("!ex hello 1", "lint=1, third=null, start=hello");
        assertResponse("!ex hhel lo 3i wo 6", "lint=6, third=3i, start=hhel lo");
        assertResponse("!ex is i2noi 2i4 sz", "Error: required [lint] but not found");
    }

    private void assertResponse(final String input, final String expected) {
        CommandEventInternal spy = mockCommand(client, input);


        doAnswer(invocation -> {
            String argument = invocation.getArgument(0);
            assertEquals(expected, argument);
            return null;
        }).when(spy).reply(anyString());

        client.getCommandEngine().handle(spy);

        verify(spy).reply(expected);
    }
}
