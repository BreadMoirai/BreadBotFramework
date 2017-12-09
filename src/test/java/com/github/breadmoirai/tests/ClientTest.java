/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.internal.event.CommandEventInternal;
import com.github.breadmoirai.breadbot.util.Emoji;
import com.github.breadmoirai.tests.commands.*;
import org.junit.Assert;
import org.junit.Test;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.awt.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.breadmoirai.tests.MockFactory.mockCommand;
import static org.mockito.Mockito.*;

public class ClientTest {


    static {
        TestLoggerFactory.getInstance().setPrintLevel(Level.INFO);
    }

    private BreadBotClient client;

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
                .associatePreprocessorFactory("string", String.class, s -> (commandObj, targetHandle, event, processorStack) -> event.reply(s))
                .createCommand(PingCommand.class)
                .setKeys("pyng")
                .applyProperty("gnyp")
                .addCommand(PingCommand.class));
        assertResponse("!pyng", "gnyp");
    }

    @Test
    public void classPreprocessorPropertyTest() {
        setupBread(bread -> bread
                .associatePreprocessorFactory("repeat", Integer.class, i -> (commandObj, targetHandle, event, processorStack) -> event.reply(IntStream.range(0, i).mapToObj(value -> "pong").collect(Collectors.joining(" "))))
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
    public void parameterPropertyTest() {
        setupBread(bread -> bread.addCommand(NameCommand.class));
        assertResponse("!name a b c", "a b c");
        assertResponse("!first a b c", "a");
        assertResponse("!last a b c", "b c");
    }

    @Test
    public void returnTypeTest() {
        setupBread(bread -> bread
                .registerResultHandler(Color.class, (command, event, result) -> event.reply(Integer.toHexString(result.getRGB())))
                .addCommand(ColorCommand::new)
                .addCommand(MirrorCommand::new));
        assertResponse("!reverse mirror", "rorrim");
        assertResponse("!color BLUE", "ff0000ff");
    }

    @Test
    public void customParameterTest() {
        setupBread(bread -> bread
                .registerArgumentMapperSimple(
                        MathCommand.Operator.class,
                        arg -> arg.matches(Pattern.compile("[+\\-*/]")),
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
                                    throw new RuntimeException();
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
                    command.setParameter(0, parser -> "James");
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

    private void setupBread(Consumer<BreadBotClientBuilder> config) {
        BreadBotClientBuilder builder = new BreadBotClientBuilder();
        config.accept(builder);
        client = builder.build();
    }

    private void assertResponse(final String input, final String expected) {
        CommandEventInternal spy = mockCommand(client, input);


        doAnswer(invocation -> {
            String argument = invocation.getArgument(0);
            if (!argument.equalsIgnoreCase(expected))
                Assert.fail(String.format("Expected: \"%s\", Actual: \"%s\"", expected, argument));
            return null;
        }).when(spy).reply(anyString());

        client.getCommandEngine().handle(spy);

        verify(spy).reply(expected);
    }


}
