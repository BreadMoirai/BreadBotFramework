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

package com.github.breadmoirai.breadbot.framework.command.internal;

import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.StringJoiner;

public class CommandRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CommandHandle.class);

    private final Object o;
    private final CommandEvent event;
    private final InvokableCommand invokableCommand;
    private final CommandParser parser;
    private final CommandHandle commandHandle;
    private final CommandResultHandler<?> resultHandler;

    CommandRunner(Object o,
                  CommandEvent event,
                  InvokableCommand invokableCommand,
                  CommandParser parser,
                  CommandHandle commandHandle,
                  CommandResultHandler<?> resultHandler) {
        this.o = o;
        this.event = event;
        this.invokableCommand = invokableCommand;
        this.parser = parser;
        this.commandHandle = commandHandle;
        this.resultHandler = resultHandler;
    }

    @Override
    public void run() {
        if (parser.mapAll()) {

            if (LOG.isDebugEnabled()) {
                final int tp = parser.getParameters().length;
                final List<CommandParameter> up = parser.getUnmappedParameters();
                if (up.isEmpty()) {
                    LOG.debug("Successfully mapped " + tp + " parameters.");
                } else {
                    final StringJoiner sj = new StringJoiner(", ");
                    for (CommandParameter parameter : up) {
                        sj.add(parameter.getName());
                    }
                    LOG.debug(String.format("Successfully mapped %d/%d parameters. Failed: [%s]", up.size() - tp, tp, sj.toString()));
                }
            }

            try {
                Object result = invokableCommand.invoke(o, parser.getResults());
                if (LOG.isDebugEnabled())
                    if (commandHandle.getDeclaringMethod().getReturnType() != Void.TYPE) {
                        LOG.debug("Command Result: " + result);
                    }
                if (result != null) {
                    CommandResultHandler.handleObject(resultHandler, commandHandle, event, result);
                }
                LOG.debug("Command Execution Completed");
                return;
            } catch (Throwable throwable) {
                if (LOG.isDebugEnabled()) {
                    LOG.error("Command Execution Failed", throwable);
                } else {
                    LOG.error("An error ocurred while invoking command:\n" + commandHandle + "\non Event:\n" + event, throwable);
                }
            }
        }
        LOG.debug("Command Execution Failed");
    }

}