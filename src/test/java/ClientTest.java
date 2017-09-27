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

import com.github.breadmoirai.bot.framework.CommandClientBuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.InterfacedEventManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import javax.security.auth.login.LoginException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClientTest {

    private static final long TEST_CHANNEL = 306868361541844993L;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(600);
    public long responseTimout = 25;

    @Test
    public void test() {
        final InterfacedEventManager eventManager = new CommandClientBuilder()
                .registerCommand(PingCommand.class)
                .buildInterfaced();


        final JDA botApi;
        try {
            botApi = new JDABuilder(AccountType.BOT)
                    .setGame(Game.of("Comprehensive Testing"))
                    .setToken("MzQwNzAzODUxNjA0NjA2OTc2.DKx47Q.1BS8uVPkd5qwXaaNMU6VCea2Ufg")
                    .setEventManager(eventManager)
                    .buildBlocking();
        } catch (LoginException | RateLimitedException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        final TestClient clientApi = new TestClient(TEST_CHANNEL);
        try {
            new JDABuilder(AccountType.CLIENT)
                    .setToken("MzYwNDgzNzMyOTUzNzU5NzQ2.DKyB6A.EGtrpiQufSXaC6q8rW-LwswAKpY")
                    .setGame(Game.of("Setup"))
                    .addEventListener(clientApi)
                    .buildBlocking();
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            throw new RuntimeException(e);
        }

        clientApi.sendMessage("!ping");

        assertThat(getResponse(clientApi, botApi).getRawContent(), is("pong"));

        clientApi.sendMessage("!PING");

        assertThat(getResponse(clientApi, botApi).getRawContent(), is("pong"));
    }

    public Message getResponse(TestClient test, JDA api) {
        final TextChannel channel = api.getTextChannelById(TEST_CHANNEL);
        int counter = 0;
        while (test.getLastMessage() == channel.getLatestMessageIdLong() && counter < responseTimout) {
            try {
                counter++;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (counter == responseTimout) throw new RuntimeException("No Response");
        return channel.getMessageById(channel.getLatestMessageId()).complete();
    }
}
