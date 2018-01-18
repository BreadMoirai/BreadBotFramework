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

package com.github.breadmoirai.breadbot.framework.parameter.internal.builder;

import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.ArgumentCollectionBuilder;
import com.github.breadmoirai.breadbot.framework.parameter.internal.ArgumentParserCollectionImpl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

public class CollectionTypes {

    private CollectionTypes() {
    }


    private static <T> void setTypeParser(Class<T> type, CommandParameterBuilder builder) {
        final BreadBotBuilder clientBuilder = builder.getClientBuilder();
        final TypeParser<T> typeParser = clientBuilder.getTypeParser(type);
        builder.setTypeParser(typeParser);
    }

    public static <T> void setParserToGenericList(Class<T> type, CommandParameterBuilderImpl builder) {
        setTypeParser(type, builder);
        builder.setArgumentParser(p -> new ArgumentParserCollectionImpl(p, () -> ArgumentCollectionBuilder.<List<T>, T>of(ArrayList::new, List::add, f -> f)));

    }

    public static <T> void setParserToGenericDeque(Class<T> type, CommandParameterBuilderImpl builder) {
        setTypeParser(type, builder);
        builder.setArgumentParser(p -> new ArgumentParserCollectionImpl(p, () -> ArgumentCollectionBuilder.<Deque<T>, T>of(ArrayDeque::new, Deque::add, f -> f)));
    }

    public static <T> void setParserToGenericStream(Class<T> type, CommandParameterBuilderImpl builder) {
        setTypeParser(type, builder);
        builder.setArgumentParser(p -> new ArgumentParserCollectionImpl(p, () -> ArgumentCollectionBuilder.<Stream.Builder<T>, T>of(Stream::<T>builder, Stream.Builder::<T>accept, Stream.Builder::build)));
    }


    public static void setParserToIntStream(CommandParameterBuilderImpl builder) {
        builder.setTypeParser(arg -> arg.isInteger() ? arg : null);
        builder.setArgumentParser(p -> new ArgumentParserCollectionImpl(p, () -> ArgumentCollectionBuilder.<Stream.Builder<CommandArgument>, CommandArgument>of(Stream::<CommandArgument>builder, Stream.Builder::accept, stream -> stream.build().mapToInt(CommandArgument::parseInt))));
    }

    public static void setParserToLongStream(CommandParameterBuilderImpl builder) {
        builder.setTypeParser(arg -> arg.isLong() ? arg : null);
        builder.setArgumentParser(p -> new ArgumentParserCollectionImpl(p, () -> ArgumentCollectionBuilder.<Stream.Builder<CommandArgument>, CommandArgument>of(Stream::<CommandArgument>builder, Stream.Builder::accept, stream -> stream.build().mapToLong(CommandArgument::parseLong))));
    }

    public static void setParserToDoubleStream(CommandParameterBuilderImpl builder) {
        builder.setTypeParser(arg -> arg.isFloat() ? arg : null);
        builder.setArgumentParser(p -> new ArgumentParserCollectionImpl(p, () -> ArgumentCollectionBuilder.<Stream.Builder<CommandArgument>, CommandArgument>of(Stream::<CommandArgument>builder, Stream.Builder::accept, stream -> stream.build().mapToDouble(CommandArgument::parseDouble))));
    }
}
