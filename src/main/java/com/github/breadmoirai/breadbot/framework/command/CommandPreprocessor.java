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

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandProcessStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandPreprocessor implements CommandPreprocessorFunction {

    private static final Logger LOG = LoggerFactory.getLogger(CommandPreprocessor.class);

    private final String identifier;
    private final CommandPreprocessorFunction function;

    public CommandPreprocessor(String identifier, CommandPreprocessorFunction function) {
        this.identifier = identifier;
        this.function = function;
    }

    public String getIdentifier() {
        return identifier;
    }

    public CommandPreprocessorFunction getFunction() {
        return function;
    }

    @Override
    public void process(Object commandObj, CommandHandle targetHandle, CommandEvent event, CommandProcessStack processQueue) {
        try {
            getFunction().process(commandObj, targetHandle, event, processQueue);
        } catch (Throwable t) {
            LOG.error("An exception was thrown while attempting to evaluate a preprocessor: " + identifier, t);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandPreprocessor that = (CommandPreprocessor) o;

        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        return function != null ? function.equals(that.function) : that.function == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (function != null ? function.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CommandPreprocessor[" + identifier + ']';
    }
}
