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

import java.lang.reflect.Parameter;
import java.util.StringJoiner;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class CommandParameterImpl implements CommandParameter {

    private final String name;
    private final Parameter parameter;
    private Class<?> type;
    private int flags;
    private int index;
    private int width;
    private ArgumentTypeMapper<?> mapper;
    private boolean mustBePresent;
    private AbsentArgumentHandler absentArgumentHandler;

    public CommandParameterImpl(String name, Parameter parameter, Class<?> type, int flags, int index, int width, ArgumentTypeMapper<?> mapper, boolean mustBePresent, AbsentArgumentHandler absentArgumentHandler) {
        this.name = name;
        this.parameter = parameter;
        this.type = type;
        this.flags = flags;
        this.index = index;
        this.width = width;
        this.mapper = mapper;
        this.mustBePresent = mustBePresent;
        this.absentArgumentHandler = absentArgumentHandler;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Parameter getDeclaringParameter() {
        return parameter;
    }

    @Override
    public Object map(CommandArgumentList list, CommandParser parser) {
        int i;
        final IntPredicate condition;
        final int increment;
        if (index > 0) {
            i = index - 1;
            if (i >= list.size())
                return null;
            final int limit = i + 1;
            condition = value -> value < limit;
            increment = 1;
        } else if (index < 0) {
            i = list.size() + index;
            if (i < 0)
                return null;
            condition = value -> value >= 0;
            increment = -1;
        } else {
            i = 0;
            condition = value -> value < list.size();
            increment = 1;
        }

        for (; condition.test(i); i += increment) {
            if (parser.hasMappedArgument(i)) continue;
            if (width == 1) {
                Object o = map(parser, list.get(i), new int[]{i});
                if (o != null) {
                    return o;
                }
            } else if (width == 0) {
                int j = i;
                for (; j < list.size(); j++) {
                    if (parser.hasMappedArgument(j)) {
                        break;
                    }
                }
                for (; j > i; j--) {
                    StringJoiner sj = new StringJoiner(" ");
                    for (int k = i; k < j; k++) {
                        sj.add(list.get(k).getArgument());
                    }
                    Object o = map(parser, sj.toString(), IntStream.range(i, j).toArray());
                    if (o != null) {
                        return o;
                    }
                }

            } else if (width < 0) {
                final StringJoiner sj = new StringJoiner(" ");
                int j = i;
                for (; j < list.size(); j++) {
                    if (parser.hasMappedArgument(j)) break;
                    sj.add(list.get(j).getArgument());
                }
                int[] indexes = IntStream.range(i, j).toArray();
                i = j;
                Object o = map(parser, sj.toString(), indexes);
                if (o != null) {
                    return o;
                }
            } else {
                final StringJoiner sj = new StringJoiner(" ");
                if (i + width > list.size())
                    continue;
                int j = i;
                for (; j < i + width; j++) {
                    if (parser.hasMappedArgument(j)) break;
                    sj.add(list.get(j).getArgument());
                }
                if (j == i + width) {
                    int[] indexes = IntStream.range(i, i + width).toArray();
                    Object o = map(parser, sj.toString(), indexes);
                    if (o != null) {
                        return o;
                    }
                }
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

    private Object map(CommandParser parser, String arg, int[] indexes) {
        return map(parser, new GenericCommandArgument(parser.getEvent(), arg), indexes);
    }

    private Object map(CommandParser parser, CommandArgument arg, int[] indexes) {
        Object o = mapper.map(arg, flags);
        if (o != null) {
            parser.markMappedArguments(indexes);
            return o;
        }
        return o;
    }
}