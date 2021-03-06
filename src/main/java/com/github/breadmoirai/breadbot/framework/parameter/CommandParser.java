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

package com.github.breadmoirai.breadbot.framework.parameter;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.event.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.List;

public class CommandParser {

    private final CommandEvent event;
    private final Command method;
    private final CommandArgumentList argumentList;
    private final CommandParameter[] parameters;
    private final TIntSet set;
    private final int size;

    private boolean fail = false;
    private int pointer;

    private final Object[] results;

    public CommandParser(CommandEvent event, Command method, CommandArgumentList argumentList, CommandParameter[] parameters) {
        this.event = event;
        this.method = method;
        this.argumentList = argumentList;
        this.parameters = parameters;
        this.set = new TIntHashSet();
        this.pointer = 0;
        this.size = parameters.length;
        this.results = new Object[size];
    }

    public CommandEvent getEvent() {
        return event;
    }

    public CommandParameter[] getParameters() {
        return Arrays.copyOf(parameters, parameters.length);
    }

    public List<CommandParameter> getUnmappedParameters() {
        return Arrays.asList(parameters).subList(pointer, size);
    }

    /**
     * The pointer points to the next CommandParameter.
     * When {@code getPointer() == size()}, all params have been mapped.
     *
     * @return and int ranging from {@code 1 - size()}.
     */
    public int getPointer() {
        return pointer;
    }

    public Command getMethod() {
        return method;
    }

    public CommandArgumentList getArgumentList() {
        return argumentList;
    }

    public boolean hasNext() {
        return pointer < size && !fail;
    }

    public void mapNext() {
        if (!fail) {
            results[pointer] = parameters[pointer].map(this);
            pointer++;
        }
    }

    /**
     * signals to halt execution of command.
     */
    public void fail() {
        fail = true;
    }

    public boolean hasFailed() {
        return fail;
    }


    public Object[] getResults() {
        return results;
    }

    public int size() {
        return size;
    }

    public boolean hasMappedArguments(int index) {
        return set.contains(index);
    }

    public boolean markMappedArguments(int index) {
        return set.add(index);
    }

    public boolean hasMappedArguments(int[] array) {
        return set.containsAll(array);
    }

    public boolean markMappedArguments(int[] array) {
        return set.addAll(array);
    }

    public boolean mapAll() {
        while (hasNext()) mapNext();
        return !hasFailed();
    }
}