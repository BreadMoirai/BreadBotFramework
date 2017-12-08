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
import com.github.breadmoirai.breadbot.framework.error.DuplicateCommandKeyException;
import com.github.breadmoirai.breadbot.framework.error.MissingCommandKeyException;
import com.github.breadmoirai.breadbot.framework.internal.event.CommandEventInternal;
import com.github.breadmoirai.tests.commands.BadCommand;
import com.github.breadmoirai.tests.commands.PingCommand;
import org.junit.Test;

import static com.github.breadmoirai.tests.MockFactory.mockCommand;

public class ExceptionTester {

    @Test(expected = MissingCommandKeyException.class)
    public void missingKey() {
        new BreadBotClientBuilder()
                .addCommand(commandEvent -> {}, configurator -> {})
                .build();
    }

    @Test(expected = DuplicateCommandKeyException.class)
    public void duplicateKey() {
        new BreadBotClientBuilder()
                .addCommand(PingCommand.class)
                .addCommand(PingCommand::new)
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void commandctorBuild() {
        new BreadBotClientBuilder()
                .addCommand(BadCommand::new)
                .build();
    }

    @Test
    public void commandctorInvoke() {
        BreadBotClient bread = new BreadBotClientBuilder()
                .addCommand(BadCommand.class)
                .build();
        CommandEventInternal mock = mockCommand(bread, "!bad");
        bread.getCommandEngine().handle(mock);
    }
}