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
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;

import java.lang.reflect.Parameter;

public class CommandParameterImpl implements CommandParameter {

    private final String name;
    private final Parameter parameter;
    private final int index;
    private final int width;
    private final int limit;
    private final boolean contiguous;
    private final ArgumentParser argumentParser;
    private final boolean mustBePresent;
    private final AbsentArgumentHandler absentArgumentHandler;

    public CommandParameterImpl(String name, Parameter parameter, int index, int width, int limit, boolean contiguous, ArgumentParser argumentParser, boolean mustBePresent, AbsentArgumentHandler absentArgumentHandler) {
        this.name = name;
        this.parameter = parameter;
        this.index = index;
        this.width = width;
        this.limit = limit;
        this.contiguous = contiguous;
        this.argumentParser = argumentParser;
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
        return argumentParser.parse(this, list, parser);
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
    public int getLimit() {
        return limit;
    }

    @Override
    public boolean isContiguous() {
        return contiguous;
    }

    @Override
    public boolean mustBePresent() {
        return mustBePresent;
    }

    @Override
    public AbsentArgumentHandler getAbsentArgumentHandler() {
        return absentArgumentHandler;
    }

}