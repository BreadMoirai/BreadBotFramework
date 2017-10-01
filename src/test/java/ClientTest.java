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

import com.github.breadmoirai.bot.framework.CommandClient;
import com.github.breadmoirai.bot.framework.CommandClientBuilder;
import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessors;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClientTest {

    private static final long TEST_CHANNEL = 306868361541844993L;
    private static final String BOT_TOKEN = "MzQwNzAzODUxNjA0NjA2OTc2.DLF18A.JhSbDlU-67yRoZX_juYFXLlW4Mg";
    private static final String CLIENT_TOKEN = "MzEzODk3NjQ1Nzc5MzIwODMy.DLF3Dg.rM1Qj5awxS8IyymE5EzPMDCSEcA";

    @Rule
    public final Timeout globalTimeout = Timeout.seconds(600);
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final static long RESPONSE_TIMEOUT = 10;

    private static ClientSender clientSender;
    private static JDA botApi, clientApi;

    private static long clientId, botId;

    private static BlockingQueue<Message> botQueue = new LinkedBlockingQueue<>();
//    private static BlockingQueue<Message> clientQueue = new LinkedBlockingQueue<>();


    @BeforeClass
    public static void setup() {
        CommandPreprocessors.associatePreprocessor(String.class, s -> new CommandPreprocessor("reversal", (commandObj, targetHandle, event, processorStack) -> event.reply(s)));
        CommandPreprocessors.associatePreprocessor(Integer.class, integer -> new CommandPreprocessor("repeater", (commandObj, targetHandle, event, processorStack) -> event.reply(IntStream.range(0, integer).mapToObj(value -> "pong").collect(Collectors.joining(" ")))));
        CommandPreprocessors.setPreprocessorPriority("alpha", "beta");
        final CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
        final InterfacedEventManager eventManager = commandClientBuilder
                .registerCommand(PingCommand.class)
                .registerCommand(PingCommand.class, builder -> builder
                        .configureCommandMethod("ping", methodBuilder -> methodBuilder
                                .setKeys("pang")
                                .addPreprocessorFunction("doubledown", (commandObj, targetHandle, event, processQueue) -> {
                                    event.reply("pung");
                                })))
                .registerCommand(PingCommand.class, builder -> builder
                        .configureCommandMethod("ping", methodBuilder -> methodBuilder
                                .setKeys("pyng")
                                .putProperty("gnyp")
                                .addAssociatedPreprocessors()))
                .registerCommand(PingCommand.class, builder -> builder
                        .configureCommandMethod("ping", methodBuilder -> methodBuilder
                                .setKeys("poing"))
                        .putProperty(Integer.valueOf(5))
                        .addAssociatedPreprocessors())
                .registerCommand(event -> event.reply("failed"), builder -> builder
                        .setKeys("pnosort")
                        .setName("pnosort")
                        .addPreprocessorFunction("beta", (commandObj, targetHandle, event, processorStack) -> event.reply("beta"))
                        .addPreprocessorFunction("alpha", (commandObj, targetHandle, event, processorStack) -> event.reply("alpha")))
                .registerCommand(event -> event.reply("failed"), builder -> builder
                        .setKeys("ponsort")
                        .setName("ponsort")
                        .addPreprocessorFunction("beta", (commandObj, targetHandle, event, processorStack) -> event.reply("beta"))
                        .addPreprocessorFunction("alpha", (commandObj, targetHandle, event, processorStack) -> event.reply("alpha"))
                        .sortPreprocessors())
                .registerCommand(CountCommand.class, builder -> builder.setPersistent(true))
                .registerCommand(CountCommand.class, builder -> builder.configureCommandMethod("count", methodBuilder -> methodBuilder.setKeys("cyunt")))
                .registerCommand(new CountCommand(10), builder -> builder.setPersistent(true).configureCommandMethod("count", methodBuilder -> methodBuilder.setKeys("coynt")))
                .registerCommand(NameCommand.class)
                .buildInterfaced();

        final CommandClient client = commandClientBuilder.getClient();

        final Map<String, CommandHandle> commandMap = client.getCommandEngine().getCommandMap();
        for (Map.Entry<String, CommandHandle> stringCommandHandleEntry : commandMap.entrySet()) {
            final String key = stringCommandHandleEntry.getKey();
            System.out.println("key = " + key);
            final CommandHandle handle = stringCommandHandleEntry.getValue();
            System.out.println("handle = " + handle);
            final List<CommandPreprocessor> preprocessors = handle.getPreprocessors();
            System.out.println("preprocessors = " + preprocessors);
        }


        try {
            botApi = new JDABuilder(AccountType.BOT)
                    .setGame(Game.of("Testing"))
                    .setToken(BOT_TOKEN)
                    .setEventManager(eventManager)
                    .addEventListener((EventListener) event -> {
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
                    })
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
        assertResponse("!ping", "pong");
    }

    @Test
    public void ignoreKeyCaseTest() {
        assertResponse("!PING", "pong");
    }

    @Test
    public void basicPreprocessorTest() {
        assertResponse("!pang", "pung");
    }

    @Test
    public void methodPreprocessorPropertyTest() {
        assertResponse("!pyng", "gnyp");
    }

    @Test
    public void classPreprocessorPropertyTest() {
        final String message = "!poing";
        final String response = "pong pong pong pong pong";
        assertResponse(message, response);
    }

    @Test
    public void preprocessorPriorityTest() {
        assertResponse("!pnosort", "beta");
        assertResponse("!ponsort", "alpha");
    }

    @Test
    public void persistenceTest() {
        assertResponse("!count", "1");
        assertResponse("!count", "2");
        assertResponse("!count", "3");
    }

    @Test
    public void impersistenceTest() {
        assertResponse("!cyunt", "1");
        assertResponse("!cyunt", "1");
    }

    @Test
    public void persistentSupplierTest() {
        assertResponse("!coynt", "10");
        assertResponse("!coynt", "11");
        assertResponse("!coynt", "12");
    }

    @Test
    public void parameterPropertyTest() {
        assertResponse("!name a b c", "a b c");
        assertResponse("!first a b c", "a");
        assertResponse("!last a b c", "b c");
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

}
