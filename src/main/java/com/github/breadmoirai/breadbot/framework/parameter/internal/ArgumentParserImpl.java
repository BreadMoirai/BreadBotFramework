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

package com.github.breadmoirai.breadbot.framework.parameter.internal;

import com.github.breadmoirai.breadbot.framework.event.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.internal.arguments.GenericCommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;

import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class ArgumentParserImpl implements ArgumentParser {

    private int index;
    private int width;
    private boolean mustBePresent;
    private AbsentArgumentHandler absentArgumentHandler;
    private TypeParser<?> typeParser;
    private Function<CommandEvent, ?> defaultValue;

    public ArgumentParserImpl(int index, int width, boolean mustBePresent, AbsentArgumentHandler absentArgumentHandler, TypeParser<?> typeParser) {
        this(index, width, mustBePresent, absentArgumentHandler, typeParser, null);
    }

    public ArgumentParserImpl(int index, int width, boolean mustBePresent, AbsentArgumentHandler absentArgumentHandler, TypeParser<?> typeParser, Function<CommandEvent, ?> defaultValue) {
        this.index = index;
        this.width = width;
        this.mustBePresent = mustBePresent;
        this.absentArgumentHandler = absentArgumentHandler;
        this.typeParser = typeParser;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object parse(CommandParameter parameter, CommandArgumentList list, CommandParser parser) {
        int i;
        final IntPredicate condition;
        final int increment;
        if (index > 0) {
            i = index - 1;
            if (i >= list.size()) {
                return getDefaultOrFail(parameter, parser);
            }
            final int limit = i + 1;
            condition = value -> value < limit;
            increment = 1;
        } else if (index < 0) {
            i = list.size() + index;
            if (i < 0)
                return getDefaultOrFail(parameter, parser);
            condition = value -> value >= 0;
            increment = -1;
        } else {
            i = 0;
            condition = value -> value < list.size();
            increment = 1;
        }

        for (; condition.test(i); i += increment) {
            if (parser.hasMappedArguments(i)) continue;
            if (width == 1) {
                CommandArgument arg = list.get(i);
                Object o = typeParser.parse(arg);
                if (o != null) {
                    parser.markMappedArguments(i);
                    return o;
                }
            } else if (width == 0) {
                int j = i;
                for (; j < list.size(); j++) {
                    if (parser.hasMappedArguments(j)) {
                        break;
                    }
                }
                for (; j > i; j--) {
                    StringJoiner sj = new StringJoiner(" ");
                    for (int k = i; k < j; k++) {
                        sj.add(list.get(k).getArgument());
                    }
                    int[] indexes = IntStream.range(i, j).toArray();
                    Object o = typeParser.parse(new GenericCommandArgument(parser.getEvent(), sj.toString()));
                    if (o != null) {
                        parser.markMappedArguments(indexes);
                        return o;
                    }
                }

            } else if (width < 0) {
                final StringJoiner sj = new StringJoiner(" ");
                int j = i;
                for (; j < list.size(); j++) {
                    if (parser.hasMappedArguments(j)) break;
                    sj.add(list.get(j).getArgument());
                }
                int[] indexes = IntStream.range(i, j).toArray();
                i = j;
                Object o = typeParser.parse(new GenericCommandArgument(parser.getEvent(), sj.toString()));
                if (o != null) {
                    parser.markMappedArguments(indexes);
                    return o;
                }
            } else {
                final StringJoiner sj = new StringJoiner(" ");
                if (i + width > list.size())
                    continue;
                int j = i;
                for (; j < i + width; j++) {
                    if (parser.hasMappedArguments(j)) break;
                    sj.add(list.get(j).getArgument());
                }
                if (j == i + width) {
                    int[] indexes = IntStream.range(i, i + width).toArray();
                    Object o = typeParser.parse(new GenericCommandArgument(parser.getEvent(), sj.toString()));
                    if (o != null) {
                        parser.markMappedArguments(indexes);
                        return o;
                    }
                }
            }

        }

        return getDefaultOrFail(parameter, parser);
    }

    private Object getDefaultOrFail(CommandParameter parameter, CommandParser parser) {
        if (defaultValue != null) {
            final Object v = defaultValue.apply(parser.getEvent());
            if (v == null && mustBePresent) parser.fail();
            if (absentArgumentHandler != null) {
                absentArgumentHandler.handle(parser.getEvent(), parameter);
            }
            return v;
        } else {
            if (mustBePresent) parser.fail();
            if (absentArgumentHandler != null) {
                absentArgumentHandler.handle(parser.getEvent(), parameter);
            }
            return null;
        }
    }

}
