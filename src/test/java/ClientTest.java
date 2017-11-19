/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.internal.BreadBotClientImpl;
import com.github.breadmoirai.breadbot.util.Emoji;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import test.commands.*;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClientTest {

    private static final long TEST_CHANNEL = 376827325960028170L;
    private static final String BOT_TOKEN;
    private static final String CLIENT_TOKEN;

    static {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        ClientTest.class.getResourceAsStream("tokens.txt"))
        )) {
            BOT_TOKEN = br.readLine();
            CLIENT_TOKEN = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Token not found", e);
        }
    }


    @Rule
    public final Timeout globalTimeout = Timeout.seconds(600);
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final static long RESPONSE_TIMEOUT = 10;

    private static ClientSender clientSender;
    private static JDA botApi, clientApi;

    private static long clientId, botId;

    private static MyEventManager manager;
    private static BlockingDeque<Message> botQueue = new LinkedBlockingDeque<>();


    @BeforeClass
    public static void setupBot() {
        manager = new MyEventManager();
        try {
            botApi = new JDABuilder(AccountType.BOT)
                    .setGame(Game.of("Testing"))
                    .setToken(BOT_TOKEN)
                    .setEventManager(manager)
                    .buildBlocking();
        } catch (LoginException | RateLimitedException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            clientApi = new JDABuilder(AccountType.CLIENT)
                    .setToken(CLIENT_TOKEN)
                    .setGame(Game.of("Testing"))
                    .buildBlocking();
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            throw new RuntimeException(e);
        }
        clientSender = new ClientSender(clientApi, TEST_CHANNEL);

        botId = botApi.getSelfUser().getIdLong();
        clientId = clientApi.getSelfUser().getIdLong();
    }

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
        assertResponse("!ping?", "test.PingCommand.class");
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
        setupBread(bread -> bread.addCommand(ColorCommand::new).addCommand(MirrorCommand::new));
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

    private void setupBread(Consumer<BreadBotClientBuilder> config) {
        BreadBotClientBuilder builder = new BreadBotClientBuilder();
        config.accept(builder);
        BreadBotClient client = builder.build();
        manager.setBread(client);
        client.setJDA(botApi);
    }

    private void assertResponse(String message, String response) {
        clientSender.sendMessage(message);
        final Message poll;
        try {
            poll = botQueue.poll(RESPONSE_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (poll == null) {
            Assert.fail("No response from Bot");
            return;
        }
        assertThat(poll.getRawContent(), is(response));
    }

    private static class MyEventManager implements IEventManager {

        private BreadBotClient client;
        private MyEventListener myEventListener;


        public void setBread(BreadBotClient client) {
            this.client = client;
            myEventListener = new MyEventListener();
        }

        @Override
        public void register(Object listener) {

        }

        @Override
        public void unregister(Object listener) {

        }

        @Override
        public void handle(Event event) {
            if (client != null) {
                ((BreadBotClientImpl) client).onEvent(event);
                myEventListener.onEvent(event);
            }
        }

        @Override
        public List<Object> getRegisteredListeners() {
            return null;
        }
    }

    private static class MyEventListener implements EventListener {
        @Override
        public void onEvent(Event event) {
            if (event instanceof GuildMessageReceivedEvent) {
                final GuildMessageReceivedEvent messageReceivedEvent = (GuildMessageReceivedEvent) event;
                final long idLong = messageReceivedEvent.getAuthor().getIdLong();
                final Message message = messageReceivedEvent.getMessage();
                if (idLong == botId) {
                    botQueue.add(message);
                } else if (idLong == clientId) {
//                                clientQueue.add(message);
                }
            }
        }
    }

}
