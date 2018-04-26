package com.github.breadmoirai.tests;

import com.github.breadmoirai.breadbot.framework.BreadBot;
import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.breadbot.plugins.prefix.PrefixPlugin;
import com.github.breadmoirai.breadbot.plugins.waiter.EventWaiterPlugin;
import com.github.breadmoirai.tests.plugins.TestPlugin;
import com.github.breadmoirai.tests.plugins.TestPluginImpl;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.breadmoirai.tests.MockFactory.mockCommand;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class InjectionTester {

    private BreadBot client;

    @Test
    public void inheritanceTest() {
        client = new BreadBotBuilder().addPlugin(new TestPluginImpl()).build();

        Assert.assertNotNull(client.getPlugin(TestPluginImpl.class));
        Assert.assertNotNull(client.getPlugin(TestPlugin.class));
        Assert.assertNull(client.getPlugin(CommandPlugin.class));
    }

    @Test
    public void pluginInjectionTest() {
        final InjectionTestCommand commandObject = new InjectionTestCommand();
        final TestPluginImpl plugin = new TestPluginImpl();
        client = new BreadBotBuilder()
                .addPlugin(plugin)
                .addCommand(commandObject)
                .enableInjection()
                .build();

        assertResponse("!call", "ok ok ok");
        assertResponse("!call inner", "ok ok ok ok");
        assertEquals(commandObject.testPlugin, plugin);
        assertEquals(commandObject.getTestPluginImpl(), plugin);
        assertNotNull(InjectionTestCommand.prefixPlugin);
        assertEquals(InjectionTestCommand.prefixPlugin.getPrefix(null), "!");
        InjectionTestCommand.prefixPlugin = null;
        assertResponse("!call", "ok ok null");
        assertNull(InjectionTestCommand.prefixPlugin);
    }

    @Test
    public void newObjInjectionTest() {
        client = new BreadBotBuilder()
                .addPlugin(new TestPluginImpl())
                .addCommand(InjectionTestCommand::new)
                .enableInjection()
                .build();

        assertResponse("!call", "ok ok ok");
        InjectionTestCommand.prefixPlugin = null;
        assertResponse("!call", "ok ok ok");
        assertResponse("!call inner", "ok ok ok ok");
    }

    @Test
    public void sameObjBySupplierTest() {
        client = new BreadBotBuilder()
                .addPlugin(new TestPluginImpl())
                .addCommand(() -> new InjectionTestCommand() {
                    @Override
                    public int hashCode() {
                        return 124125;
                    }
                })
                .enableInjection()
                .build();

        assertResponse("!call", "ok ok ok");
        InjectionTestCommand.prefixPlugin = null;
        assertResponse("!call", "ok ok null");
        assertResponse("!call inner", "ok ok null ok");
    }

    public static class InjectionTestCommand {

        @Inject
        private TestPluginImpl testPluginImpl;
        @Inject
        public TestPlugin testPlugin;
        @Inject
        public static PrefixPlugin prefixPlugin;

        @MainCommand
        public String call() {
            return Stream.of(testPluginImpl, testPlugin, prefixPlugin).map(o -> o == null ? "null" : "ok").collect(
                    Collectors.joining(" "));
        }

        public TestPluginImpl getTestPluginImpl() {
            return testPluginImpl;
        }

        public class InnerInjection {

            @Inject
            public EventWaiterPlugin eventWaiter;

            @MainCommand
            public String inner() {
                return call() + (eventWaiter == null ? " null" : " ok");
            }
        }
    }

    private void assertResponse(final String input, final String expected) {
        CommandEventInternal spy = mockCommand(client, input, MockFactory.UserType.BASIC);

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