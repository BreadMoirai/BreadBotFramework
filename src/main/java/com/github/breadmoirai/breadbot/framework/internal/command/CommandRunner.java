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

package com.github.breadmoirai.breadbot.framework.internal.command;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;

import java.util.function.Consumer;

public class CommandRunner implements Runnable {

    private final Object o;
    private final CommandEvent event;
    private final InvokableCommand invokableCommand;
    private final CommandParser parser;
    private final CommandHandle commandHandle;
    private final CommandResultHandler<?> resultHandler;
    private Consumer<Throwable> onException;

    CommandRunner(Object o,
                  CommandEvent event,
                  InvokableCommand invokableCommand,
                  CommandParser parser,
                  CommandHandle commandHandle,
                  CommandResultHandler<?> resultHandler,
                  Consumer<Throwable> onException) {
        this.o = o;
        this.event = event;
        this.invokableCommand = invokableCommand;
        this.parser = parser;
        this.commandHandle = commandHandle;
        //noinspection unchecked
        this.resultHandler = resultHandler;
        this.onException = onException;
    }

    @Override
    public void run() {
        if (parser.mapAll())
            try {
                Object result = invokableCommand.invoke(o, parser.getResults());
                if (result != null) {
                    CommandResultHandler.handleObject(resultHandler, commandHandle, event, result);
                }
            } catch (Throwable throwable) {
                onException.accept(throwable);
            }
    }

}