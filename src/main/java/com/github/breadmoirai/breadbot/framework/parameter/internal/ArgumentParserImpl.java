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

import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.arguments.GenericCommandArgument;

import java.util.StringJoiner;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class ArgumentParserImpl implements ArgumentParser {

    private int index;
    private int width;
    private boolean mustBePresent;
    private AbsentArgumentHandler absentArgumentHandler;
    private TypeParser<?> typeParser;

    public ArgumentParserImpl(int index, int width, boolean mustBePresent, AbsentArgumentHandler absentArgumentHandler, TypeParser<?> typeParser) {
        this.index = index;
        this.width = width;
        this.mustBePresent = mustBePresent;
        this.absentArgumentHandler = absentArgumentHandler;
        this.typeParser = typeParser;
    }

    @Override
    public Object parse(CommandParameter parameter, CommandArgumentList list, CommandParser parser) {
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

        if (mustBePresent) parser.fail();
        if (absentArgumentHandler != null)
            absentArgumentHandler.handle(list.getEvent(), parameter);
        return null;
    }

}
