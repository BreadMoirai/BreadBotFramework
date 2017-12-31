package com.github.breadmoirai.breadbot.framework.parameter.internal.builder;

import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParserFlags;
import com.github.breadmoirai.breadbot.framework.parameter.internal.collections.ArgumentCollectionBuilder;
import com.github.breadmoirai.breadbot.framework.parameter.internal.collections.ArgumentParserCollectionImpl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CollectionParserFactory {

    private CollectionParserFactory() {
    }


    private static <T> void setTypeParser(Class<T> type, CommandParameterBuilderImpl builder) {
        if (builder.getParser() == null) {
            final BreadBotClientBuilder clientBuilder = builder.getClientBuilder();
            final TypeParser<T> typeParser = clientBuilder.getTypeParser(type);
            builder.setTypeParser(typeParser);
        }
    }

    static <T> void setParserToGenericList(Class<T> type, CommandParameterBuilderImpl builder) {
        setTypeParser(type, builder);
        final ArgumentCollectionBuilder collectionBuilder = ArgumentCollectionBuilder.<List<T>, T>of(ArrayList::new, List::add, f -> f);
        builder.setParser(new ArgumentParserCollectionImpl(collectionBuilder));
    }

    static <T> void setParserToGenericDeque(Class<T> type, CommandParameterBuilderImpl builder) {
        setTypeParser(type, builder);
        final ArgumentCollectionBuilder collectionBuilder = ArgumentCollectionBuilder.<Deque<T>, T>of(ArrayDeque::new, Deque::add, f -> f);
        builder.setParser(new ArgumentParserCollectionImpl(collectionBuilder));
    }

    public static <T> void setParserToGenericStream(Class<T> type, CommandParameterBuilderImpl builder) {
        setTypeParser(type, builder);
        final ArgumentCollectionBuilder collectionBuilder = ArgumentCollectionBuilder.<Stream.Builder<T>, T>of(Stream::<T>builder, Stream.Builder::<T>accept, Stream.Builder::build);
        builder.setParser(new ArgumentParserCollectionImpl(collectionBuilder));
    }


    public static <T> void setParserToIntStream(CommandParameterBuilderImpl builder) {

        builder.setTypeParser((arg, flags) -> {
            final boolean hex = TypeParserFlags.has(flags, TypeParserFlags.HEX);
            if (hex) {
                if (arg.isHex()) {
                    return arg;
                } else {
                    return null;
                }
            } else {
                if (arg.isInteger()) {
                    return arg;
                } else {
                    return null;
                }
            }
        });

        final ArgumentCollectionBuilder collectionBuilder = ArgumentCollectionBuilder.<IntStream.Builder, CommandArgument>of(IntStream::builder, (builder1, commandArgument) -> builder1.accept(commandArgument.parseInt()));

    }
}
