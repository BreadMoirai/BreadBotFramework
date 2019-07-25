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

package com.github.breadmoirai.breadbot.framework.event.internal;

import com.github.breadmoirai.breadbot.framework.event.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;

import java.util.RandomAccess;

/**
 * A list implementation for {@link CommandArgument CommandArguments} that includes a specialized iterator.
 */
public class CommandArgumentArrayList extends CommandArgumentList implements RandomAccess {

    private final CommandArgument[] arguments;

    public CommandArgumentArrayList(CommandArgument[] arguments, CommandEvent event) {
        super(event);
        this.arguments = arguments;
    }

    @Override
    public int size() {
        return arguments.length;
    }

    @Override
    public CommandArgument get(int index) {
        return arguments[index];
    }
}
