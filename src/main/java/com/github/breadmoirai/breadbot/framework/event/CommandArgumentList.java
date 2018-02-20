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

package com.github.breadmoirai.breadbot.framework.event;

import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;

import java.util.AbstractList;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A list implementation for {@link CommandArgument CommandArguments} that includes a specialized iterator.
 */
public abstract class CommandArgumentList extends AbstractList<CommandArgument> {

    private final CommandEvent event;

    public CommandArgumentList(CommandEvent event) {
        this.event = event;
    }

    public CommandEvent getEvent() {
        return event;
    }

    @Override
    public Spliterator<CommandArgument> spliterator() {
        return Spliterators.spliterator(this,
                                        Spliterator.SIZED
                                                | Spliterator.IMMUTABLE
                                                | Spliterator.NONNULL
                                                | Spliterator.ORDERED);
    }

    /**
     * Takes all the arguments of {@code int} and {@code range} and creates an {@link java.util.stream.IntStream}.
     *
     * @return an ordered {@link java.util.stream.IntStream} in the order the user provided.
     */
    public IntStream ints() {
        return stream().flatMapToInt(CommandArgument::parseRange);
    }

    public <T> Stream<T> stream(Class<T> type) {
        return stream().map(commandArgument -> commandArgument.getAsType(type)).filter(Objects::nonNull);
    }

    @Override
    public CommandArgumentList subList(int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                                       ") > toIndex(" + toIndex + ")");
        return this instanceof RandomAccess
                ? new SubList(fromIndex, toIndex)
                : new RandomAccessSubList(fromIndex, toIndex);
    }

    private class SubList extends CommandArgumentList {

        private int offset;
        private int limit;

        private SubList(int offset, int limit) {
            super(event);
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public CommandArgument get(int index) {
            if (index + offset > limit)
                throw new IndexOutOfBoundsException("index = " + index + " > size = " + size());
            return get(index + offset);
        }

        @Override
        public int size() {
            return limit - offset;
        }
    }

    private class RandomAccessSubList extends SubList implements RandomAccess {

        private RandomAccessSubList(int offset, int limit) {
            super(offset, limit);
        }
    }
}
