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

import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CommandParameterBuilderImpl;

import java.util.function.Supplier;

public class ArgumentParserCollectionImpl implements ArgumentParser {

    private final ArgumentParser baseParser;
    private final Supplier<ArgumentCollectionBuilder> collectorFactory;
    private final int limit;
    private final boolean contiguous;

    public ArgumentParserCollectionImpl(CommandParameterBuilderImpl param, Supplier<ArgumentCollectionBuilder> collectorFactory) {
        this.collectorFactory = collectorFactory;
        this.baseParser = new ArgumentParserImpl(param.getIndex(), param.getWidth(), false, null, param.getTypeParser());
        this.limit = param.getLimit();
        this.contiguous = param.isContiguous();
    }

    @Override
    public Object parse(CommandParameter parameter, CommandArgumentList list, CommandParser parser) {
        boolean contiguous = parameter.isContiguous();
        final int limit = parameter.getLimit();
        int count = 0;
        final ArgumentCollectionBuilder collector = collectorFactory.get();

        if (!contiguous) {
            Object o;
            while ((limit < 0 || count < limit) &&
                    (o = baseParser.parse(parameter, list, parser)) != null) {
                collector.accept(o);
                count++;
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (parser.hasMappedArguments(i)) continue;
                int j = i;
                Object o;
                while (j < list.size() &&
                        !parser.hasMappedArguments(j) &&
                        (limit < 0 || count < limit) &&
                        (o = baseParser.parse(parameter, list.subList(j, j + 1), parser)) != null) {
                    collector.accept(o);
                    j++;
                    count++;
                }
                if (j > i) break;
            }
        }
        return collector.build();
    }

}
