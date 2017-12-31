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

package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.command.internal.CommandProcessStack;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

/**
 * This functional interface is ran before the command is executed.
 * <p>
 * <b>{@link CommandPreprocessorFunction#process REFER HERE}</b>
 *
 * <p>An example implementation is shown as follows
 * <pre><code>
 *     (commandObj, targetHandle, event, processorStack) -> {
 *         //examples of criteria
 *         if (commandObj.getClass().getName().startsWith("com.github.breadmoirai.bot.commands.misc")) {
 *             event.reply("this command is from the misc package");
 *         }
 *         if (targetHandle.getGroup().equals("misc")) {
 *             event.reply("this command is from the misc group");
 *         }
 *         if (event.getMember().getColor().equals(Color.GREEN)) {
 *             event.reply("you are green");
 *         }
 *
 *         if (ThreadLocalRandom.current().nextBoolean()) {
 *             //this next line will run the next preprocessor or the command if no preprocessors are left
 *             processorStack.runNext();
 *         }
 *     }
 * </code></pre>
 */
@FunctionalInterface
public interface CommandPreprocessorFunction {

    /**
     * This method is ran before the command is executed. If you wish to continue the execution of the command, simply use the {@link java.lang.Runnable} passed and call {@link java.lang.Runnable#run() Runnable#run()} to trigger the next preprocessor or the activation of the command.
     * Calling the passed {@link java.lang.Runnable} again after the first invocation will have no effect.
     *
     * @param commandObj   The command object that will be used to call the commandHandle.
     * @param targetHandle The targetHandle. This is whatever the annotation was attached to.
     * @param event        the event that triggered things
     * @param processorStack A queue of Runnables that represent any other preprocessors and the last element being the command itself. Failure to call {@code processQueue.continue()}  will result in the command not being activated.
     */
    void process(Object commandObj, CommandHandle targetHandle, CommandEvent event, CommandProcessStack processorStack);

}