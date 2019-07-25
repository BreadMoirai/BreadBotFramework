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

package com.github.breadmoirai.breadbot.framework.command.internal;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * This is an ArrayDeque. Intended to be used as a stack. Generally the only important method is {@link #runNext()}
 * Failure to call runNext within a method will stop command execution.
 */
public class CommandProcessStack extends ArrayDeque<CommandPreprocessor> {

    private final Object object;
    private final Command targetHandle;
    private final CommandEvent event;
    private final Runnable onEnd;
    private boolean ranEnd;

    public CommandProcessStack(Object object, Command targetHandle, CommandEvent event, Collection<CommandPreprocessor> preprocessors, Runnable onEnd) {
        super(preprocessors);
        this.object = object;
        this.targetHandle = targetHandle;
        this.event = event;
        this.onEnd = onEnd;
        this.ranEnd = false;
    }

    public Object getCommandObject() {
        return object;
    }

    public Command getTargetHandle() {
        return targetHandle;
    }

    public CommandEvent getEvent() {
        return event;
    }

    /**
     * It is generally recommended to use this method to continue operation. Calling this method when this stack is empty will run the command.
     */
    public void runNext() {
        if (!this.isEmpty())
            this.pop().process(object, targetHandle, event, this);
        else {
            onEnd.run();
            ranEnd = true;
        }
    }

    /**
     * Returns {@code true} if the command has been executed.
     *
     * @return If you're calling this from a preprocessor, it will return {@code false}
     */
    public boolean result() {
        return ranEnd;
    }
}
