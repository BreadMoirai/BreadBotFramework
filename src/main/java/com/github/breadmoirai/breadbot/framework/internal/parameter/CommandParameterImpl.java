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

package com.github.breadmoirai.breadbot.framework.internal.parameter;

import com.github.breadmoirai.breadbot.framework.internal.parameter.arguments.GenericCommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.*;

import java.util.StringJoiner;
import java.util.stream.IntStream;

public class CommandParameterImpl implements CommandParameter {

    private Class<?> type;
    private int flags;
    private int index;
    private int width;
    private ArgumentTypeMapper<?> mapper;
    private boolean mustBePresent;
    private AbsentArgumentHandler absentArgumentHandler;

    public CommandParameterImpl(Class<?> type, int flags, int index, int width, ArgumentTypeMapper<?> mapper, boolean mustBePresent, AbsentArgumentHandler absentArgumentHandler) {
        this.type = type;
        this.flags = flags;
        this.index = index;
        this.width = width;
        this.mapper = mapper;
        this.mustBePresent = mustBePresent;
        this.absentArgumentHandler = absentArgumentHandler;
    }

    @Override
    public Object map(CommandArgumentList list, CommandParser parser) {
        int i;
        final int limit;
        if (index >= 0) {
            i = index;
            limit = i + 1;
        } else {
            i = 0;
            limit = list.size();
        }

        for (; i < limit; i++) {
            if (parser.hasMappedArgument(i)) continue;
            int[] indexes;
            final CommandArgument commandArgument;
            if (width == 1) {
                indexes = new int[]{i};
                commandArgument = list.get(i);
            } else if (width < 1) {
                final StringJoiner sj = new StringJoiner(" ");
                int j = i;
                for (; j < list.size(); j++) {
                    if (parser.hasMappedArgument(j)) break;
                    sj.add(list.get(j).getArgument());
                }
                indexes = IntStream.range(i, j).toArray();
                commandArgument = new GenericCommandArgument(parser.getEvent(), sj.toString());
            } else {
                final StringJoiner sj = new StringJoiner(" ");
                if (i + width >= list.size())
                    continue;
                for (int j = i; j < i + width && j < list.size(); j++) {
                    if (parser.hasMappedArgument(i)) break;
                    sj.add(list.get(i).getArgument());
                }
                indexes = IntStream.range(i, i + width).toArray();
                commandArgument = new GenericCommandArgument(parser.getEvent(), sj.toString());
            }
            final Object o = mapper.map(commandArgument, flags);
            if (o != null) {
                parser.markMappedArguments(indexes);
                return o;
            }
        }

        if (mustBePresent) parser.fail();
        if (absentArgumentHandler != null)
            absentArgumentHandler.handle(list.getEvent(), this);
        return null;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public boolean isMustBePresent() {
        return mustBePresent;
    }

}