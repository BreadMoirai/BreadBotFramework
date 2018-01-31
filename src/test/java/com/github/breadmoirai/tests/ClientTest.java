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
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.command.AbstractCommand;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.breadbot.util.Emoji;
import com.github.breadmoirai.tests.commands.ColorCommand;
import com.github.breadmoirai.tests.commands.CountCommand;
import com.github.breadmoirai.tests.commands.EmojiCommand;
import com.github.breadmoirai.tests.commands.HelpCommand;
import com.github.breadmoirai.tests.commands.MathCommand;
import com.github.breadmoirai.tests.commands.MirrorCommand;
import com.github.breadmoirai.tests.commands.NameCommand;
import com.github.breadmoirai.tests.commands.PingCommand;
import com.github.breadmoirai.tests.commands.StaticCommand;
import com.github.breadmoirai.tests.commands.TypeTestKeyTestCommand;
import org.junit.Test;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.awt.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.breadmoirai.tests.MockFactory.mockCommand;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ClientTest {


    static {
        TestLoggerFactory.getInstance().setPrintLevel(Level.DEBUG);
    }

    private BreadBot client;

    @Test
    public void basicCommandTest() {
        setupBread(builder -> builder.addCommand(PingCommand.class));
        assertResponse("!ping", "pong");
        setupBread(builder -> builder.addCommand(PingCommand::new));
        assertResponse("!ping", "pong");
        setupBread(builder -> builder.addCommand(new PingCommand()));
        assertResponse("!ping", "pong");
    }

    @Test
    public void basicContentTest() {
        setupBread(builder ->
                builder.createCommand(PingCommand.class)
                        .setKeys("p1ng")
                        .addPreprocessorFunction("1st", (commandObj, targetHandle, event, processorStack) -> {
                            System.out.println("targetHandle = " + targetHandle);
                            System.out.println("event = " + event);
                            assertEquals("the first", event.getContent());
                            assertArrayEquals(new String[]{"p1ng"}, event.getKeys());
                            processorStack.runNext();
                        })
                        .createCommand(PingCommand.class)
                        .setKeys("p2ng")
                        .addPreprocessorFunction("2nd", (commandObj, targetHandle, event, processorStack) -> {
                            System.out.println("targetHandle = " + targetHandle);
                            System.out.println("event = " + event);
                            assertEquals("the second", event.getContent());
                            assertArrayEquals(new String[]{"p1ng", "p2ng"}, event.getKeys());
                            processorStack.runNext();
                        })
                        .createCommand(PingCommand.class)
                        .setKeys("p3ng")
                        .addPreprocessorFunction("3rd", (commandObj, targetHandle, event, processorStack) -> {
                            System.out.println("targetHandle = " + targetHandle);
                            System.out.println("event = " + event);
                            assertEquals("the third", event.getContent());
                            assertArrayEquals(new String[]{"p1ng", "p2ng", "p3ng"}, event.getKeys());
                            processorStack.runNext();
                        }));
        assertResponse("!p1ng the first", "pong");
        assertResponse("!p1ng p2ng the second", "pong");
        assertResponse("!p1ng p2ng p3ng the third", "pong");
    }

    @Test
    public void ignoreKeyCaseTest() {
        setupBread(builder -> builder.addCommand(PingCommand.class));
        assertResponse("!PING", "pong");
    }

    @Test
    public void ignoreSpacePrefix() {
        setupBread(builder -> builder.addCommand(PingCommand::new));
        assertResponse("! ping", "pong");
    }

    @Test
    public void basicPreprocessorTest() {
        setupBread(bread -> bread.addCommand(PingCommand.class, builder -> builder
                .setKeys("pang")
                .addPreprocessorFunction("doubledown", (commandObj, targetHandle, event, processQueue) -> {
                    event.reply("pung");
                })));
        assertResponse("!pang", "pung");
    }

    @Test
    public void preprocessorCommandObjectAccessTest() {
        setupBread(bread -> bread
                .addCommand(PingCommand.class, handle -> handle
                        .setKeys("ping?")
                        .addPreprocessorFunction("identify", (commandObj, targetHandle, event, processorStack) -> {
                            event.reply(targetHandle.getGroup() + "." + commandObj.getClass().getSimpleName() + ".class");
                        })
                ));
        assertResponse("!ping?", "tests.PingCommand.class");
    }

    @Test
    public void methodPreprocessorPropertyTest() {
        setupBread(bread -> bread
                .bindPreprocessorFactory("string", String.class, s -> (commandObj, targetHandle, event, processorStack) -> event.reply(s))
                .createCommand(PingCommand.class)
                .setKeys("pyng")
                .applyProperty("gnyp")
                .addCommand(PingCommand.class));
        assertResponse("!pyng", "gnyp");
    }

    @Test
    public void classPreprocessorPropertyTest() {
        setupBread(bread -> bread
                .bindPreprocessorFactory("repeat", Integer.class, i -> (commandObj, targetHandle, event, processorStack) -> event.reply(IntStream.range(0, i).mapToObj(value -> "pong").collect(Collectors.joining(" "))))
                .addCommand(PingCommand.class, builder -> builder
                        .setKeys("poing")
                        .applyProperty(5))
        );
        final String message = "!poing";
        final String response = "pong pong pong pong pong";
        assertResponse(message, response);
    }

    @Test
    public void preprocessorPriorityTest() {
        setupBread(bread -> bread
                .setPreprocessorPriority("alpha", "beta")
                .addCommand(event -> event.reply("failed"), builder -> builder
                        .setKeys("pnosort")
                        .setName("pnosort")
                        .addPreprocessorFunction("beta", (commandObj, targetHandle, event, processorStack) -> event.reply("beta"))
                        .addPreprocessorFunction("alpha", (commandObj, targetHandle, event, processorStack) -> event.reply("alpha")))
                .addCommand(event -> event.reply("failed"), builder -> builder
                        .setKeys("ponsort")
                        .setName("ponsort")
                        .addPreprocessorFunction("beta", (commandObj, targetHandle, event, processorStack) -> event.reply("beta"))
                        .addPreprocessorFunction("alpha", (commandObj, targetHandle, event, processorStack) -> event.reply("alpha"))
                        .sortPreprocessors())
        );
        assertResponse("!pnosort", "beta");
        assertResponse("!ponsort", "alpha");
    }

    @Test
    public void persistenceTest() {
        setupBread(bread -> bread
                .addCommand(CountCommand.class, builder -> builder.setPersistent(true))
                .addCommand(CountCommand.class, builder -> builder.setKeys("cyunt"))
                .addCommand(() -> new CountCommand(10), builder -> builder
                        .setPersistent(true)
                        .setKeys("coynt")));
        assertResponse("!count", "1");
        assertResponse("!count", "2");
        assertResponse("!count", "3");
        assertResponse("!cyunt", "1");
        assertResponse("!cyunt", "1");
        assertResponse("!coynt", "10");
        assertResponse("!coynt", "11");
        assertResponse("!coynt", "12");
    }

    @Test
    public void returnTypeTest() {
        setupBread(bread -> bread
                .bindResultHandler(Color.class, (command, event, result) -> event.reply(Integer.toHexString(result.getRGB())))
                .addCommand(ColorCommand::new)
                .addCommand(MirrorCommand::new));
        assertResponse("!reverse mirror", "rorrim");
        assertResponse("!color BLUE", "ff0000ff");
    }

    @Test
    public void customParameterTest() {
        setupBread(bread -> bread
                .bindTypeParser(
                        MathCommand.Operator.class,
                        arg -> {
                            switch (arg.getArgument()) {
                                case "+":
                                    return new MathCommand.AddOperator();
                                case "-":
                                    return new MathCommand.SubtractOperator();
                                case "/":
                                    return new MathCommand.DivideOperator();
                                case "*":
                                    return new MathCommand.MultiplyOperator();
                                default:
                                    return null;
                            }
                        })
                .addCommand(MathCommand::new));
        assertResponse("!math 1 + 2 = 3", "invalid");
        assertResponse("!math 1 + 2", "3.0");
        assertResponse("!math   1    * 2 / 3 + 2 * 3 - 11.5", "-3.5");
    }

    @Test
    public void emojiTest() {
        setupBread(bread -> bread.addCommand(new EmojiCommand()));
        assertResponse("!emoji 5", "missing parameter: Emoji");
        assertResponse("!emoji " + Emoji.ANGER_RIGHT, Emoji.ANGER_RIGHT.getUrl());
        assertResponse("!emoji name " + Emoji.ARTICULATED_LORRY, "ARTICULATED LORRY");
    }

    @Test
    public void subCommandTest() {
        setupBread(bread -> {
            CommandHandleBuilder command = bread.createCommand(new StaticCommand("key0", "0"));
            for (int i = 1; i < 5; i++) {
                command = command.createCommand(new StaticCommand("key" + i, String.valueOf(i)));
            }
        });

        assertResponse("!key0", "0");
        assertResponse("!key0 key1 key2 key3 key4", "4");
        assertResponse("!key0 key1 key3 key4", "1");
    }

    @Test
    public void helpTest() {
        setupBread(bread -> bread.addCommand(HelpCommand::new));
        assertResponse("!help", "sure");
        assertResponse("!help awgnaoiwg", "sure");
        assertResponse("!help me boi", "maybe");
        assertResponse("!me help", "maybe");
    }

    @Test
    public void parameterOverrideTest() {
        setupBread(bread -> {
            for (CommandHandleBuilder command : bread.createCommands(NameCommand::new)) {
                if (command.getName().equals("name")) {
                    command.getParameter(0).setParser((parameter, list, parser) -> "James");
                }
            }
        });
        assertResponse("!name Mary Shellstrop", "James");
    }

    @Test
    public void typeTest() {
        setupBread(bread -> bread.addCommand(TypeTestKeyTestCommand::new));
        assertResponse("!10plus 2", "12");
        assertResponse("!10plus 10", "21");
    }

    @Test
    public void abstractCommandTest() {
        setupBread(bread -> bread.addCommand(new AbstractCommand() {
            {
                this.keys = new String[]{"test"};
            }

            @Override
            public void onCommand(CommandEvent event) {
                event.reply("am abstract");
            }
        }));

        assertResponse("!test", "am abstract");
    }

    @Test
    public void retroactiveModifierTest() {
        setupBread(bread -> bread.addCommand(PingCommand::new)
                .bindCommandModifier(null, (o, c) -> c.addPreprocessorFunction("sniper", (commandObj, targetHandle, event, processorStack) -> event.reply("bang!"))));
        assertResponse("!ping", "bang!");
    }

    private void setupBread(Consumer<BreadBotBuilder> config) {
        BreadBotBuilder builder = new BreadBotBuilder();
        config.accept(builder);
        client = builder.build();
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
