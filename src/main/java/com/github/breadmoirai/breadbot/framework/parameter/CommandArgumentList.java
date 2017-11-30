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

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.internal.parameter.CommandArgumentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A list implementation for {@link CommandArgument CommandArguments} that includes a specialized iterator.
 */
public class CommandArgumentList extends AbstractList<CommandArgument> implements RandomAccess {

    private final CommandArgument[] arguments;
    private final CommandEvent event;

    public CommandArgumentList(String[] strings, CommandEvent event) {
        this.arguments = new CommandArgument[strings.length];
        this.event = event;
        CommandArgumentFactory factory = new CommandArgumentFactory(event);
        Arrays.parallelSetAll(arguments, value -> factory.parse(strings[value]));
    }

    public CommandArgumentList(CommandArgument[] arguments, CommandEvent event) {
        this.arguments = arguments;
        this.event = event;
    }

    public CommandEvent getEvent() {
        return event;
    }

    /**
     * <p>Lazily evaluates and returns the value at the given index.
     * <p>
     * Prior to retrieving an argument,
     *
     * @param index the index of the argument starting at 0.
     * @return non-null CommandArgument
     */
    @Override
    public CommandArgument get(int index) {
        return arguments[index];
    }

    /**
     * This method does not provide a new list with this list as a backing list.
     * Instead this method copies the specified range of elements into a new CommandArgumentList.
     * Changes in the new list will not be reflected in this list as both lists are unmodifiable.
     *
     * @param fromIndex the beginning index, inclusive
     * @param toIndex   the end index, exclusive
     * @throws IndexOutOfBoundsException if an endpoint index value is out of range
     *                                   {@code (fromIndex < 0 || toIndex > size)}
     * @throws IllegalArgumentException  if the endpoint indices are out of order
     *                                   {@code (fromIndex > toIndex)}
     */
    @NotNull
    @Override
    public CommandArgumentList subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size()) throw new IndexOutOfBoundsException();
        if (fromIndex > toIndex) throw new IllegalArgumentException("endpoint indices are out of order");
        return new CommandArgumentList(Arrays.copyOfRange(arguments, fromIndex, toIndex), event);
    }

    @Override
    public int size() {
        return arguments.length;
    }

    @Override
    public Spliterator<CommandArgument> spliterator() {
        return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
    }

    /**
     * Creates a {@link java.util.Spliterator} using {@link CommandArgumentList#typeIterator} as the base.
     *
     * @param type the {@link java.util.Spliterator} will only see {@link CommandArgument CommandArguments} that satisfy at least one of the types provided.
     * @return a {@link java.util.Spliterator} of type CommandArgument.
     */
    public <T> Spliterator<T> spliterator(Class<T> type) {
        return Spliterators.spliteratorUnknownSize(typeIterator(type), Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
    }

    /**
     * Attempts to match each {@link CommandArgument} to a {@link ArgumentTypeMapper} in the order passed.
     * Will automatically fail if the amount of {@link ArgumentTypeMapper ArgumentTypes} provided is greater than the amount of arguments passed.
     *
     * @param types the types of which to look for.
     * @return {@code true} if the {@link ArgumentTypeMapper ArgumentTypes} provided match the {@link CommandArgument CommandArguments} in this list with {@link CommandArgument#isOfType(Class)}
     */
    public boolean matchesType(Class<?>... types) {
        return matchesType(0, types);
    }

    /**
     * Attempts to match each {@link CommandArgument} to a {@link ArgumentTypeMapper} in the order passed.
     * Will automatically fail if the amount of {@link ArgumentTypeMapper ArgumentTypes} provided is greater than the amount of arguments passed.
     *
     * @param startIndex the starting index of which to start matching Types.
     * @param types      the types of which to look for.
     * @return {@code true} if the {@link ArgumentTypeMapper ArgumentTypes} provided match the {@link CommandArgument CommandArguments} in this list with {@link CommandArgument#isOfType(Class)}
     */
    public boolean matchesType(int startIndex, Class<?>... types) {
        if (startIndex + types.length > size()) return false;
        for (int j = 0; j < types.length; j++) {
            if (!get(j + startIndex).isOfType(types[j])) return false;
        }
        return true;
    }

    /**
     * Finds the index of the first argument that matches the specified type.
     *
     * @param type The {@link ArgumentTypeMapper} to search for.
     * @return The index of the argument if found. If none of the arguments match the type provided, {@code -1} is returned.
     */
    public int indexOfType(Class<?> type) {
        return indexOfType(0, type);
    }

    /**
     * Finds the first argument of the specified type that occurs after the index given.
     *
     * @param startIndex The index from which to start searching.
     * @param type       The {@link ArgumentTypeMapper} to search for.
     * @return The index of the argument if found. If none of the arguments match the type provided, {@code -1} is returned. If the {@code startIndex} provided is less than {@code 0}, it will be treated as {@code 0}. If the {@code startIndex} provided is greater than or equal to the size of this list, {@code -1} will be returned.
     */
    public int indexOfType(int startIndex, Class<?> type) {
        for (int i = startIndex; i < size(); i++) {
            if (get(i).isOfType(type)) return i;
        }
        return -1;
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
        return StreamSupport.stream(spliterator(type), false);
    }
//
//    public ArgumentIterator argumentIterator() {
//        return new ArgumentIterator();
//    }

    /**
     * returns an iterator that only checks for commandArguments that can be mapped to the passed type
     *
     * @param type the class of the type
     * @param <T>  the type
     * @return an iterator
     */
    public <T> ArgumentTypeIterator<T> typeIterator(Class<T> type) {
        return new ArgumentTypeIterator<>(type);
    }

    public class ArgumentTypeIterator<T> implements Iterator<T> {
        private int cursor;
        private final Class<T> type;

        private ArgumentTypeIterator(Class<T> type) {
            this.type = type;
        }

        /**
         * Note that in this specific implementation, depending on the parser for the type, this method may be redundant if the parser does not implement a {@link ArgumentParser#test(CommandArgument)} predicate
         *
         * <p>{@inheritDoc}
         *
         * <p>Note that {@link #next()} will <b>not</b> throw an exception if there are no more elements
         * @return
         */
        @Override
        public boolean hasNext() {
            for (; cursor < size(); cursor++) {
                CommandArgument commandArgument = get(cursor);
                if (commandArgument.isOfType(type)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public T next() {
            for (; cursor < size(); cursor++) {
                CommandArgument commandArgument = get(cursor);
                T asType = commandArgument.getAsType(type);
                if (asType != null) {
                    cursor++;
                    return asType;
                }
            }
            return null;
        }
    }


//    public class ArgumentIterator {
//
//        private final TObjectIntMap<Class<?>> cursorMap;
//
//        private ArgumentIterator() {
//            cursorMap = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);
//        }
//
//        /**
//         * Returns the index of the element that would be returned by a
//         * subsequent call to {@link #next}. (Returns {@code -1} if the list
//         * iterator is at the end of the list.)
//         *
//         * @return the index of the element that would be returned by a
//         * subsequent call to {@code next}, or {@code -1} if the list
//         * iterator is at the end of the list
//         */
//        public int nextIndex(Class<?> type) {
//            final int cursor = this.cursorMap.putIfAbsent(type, 0);
//            return indexOfType(cursor, type);
//        }
//
//        /**
//         * Returns {@code true} if this list iterator has more elements when
//         * traversing the list in the forward direction. (In other words,
//         * returns {@code true} if {@link #next} would return an element rather
//         * than throwing an exception.)
//         *
//         * @param type the class of type of CommandArgument to be used by it's corresponding {@link ArgumentParser#test(CommandArgument)}
//         * @return {@code true} if the list iterator has more elements when
//         * traversing the list in the forward direction
//         */
//        public boolean hasNext(Class<?> type) {
//            return nextIndex(type) != -1;
//        }
//
//        /**
//         * Returns the next element in the list and advances the cursor position.
//         * This method may be called repeatedly to iterate through the list.
//         *
//         * @param  type the class of type of CommandArgument to be used by it's corresponding {@link ArgumentParser#test(CommandArgument)}
//         * @return the next CommandArgument that is of this type.
//         * @throws NoSuchElementException if a commandArgument
//         */
//        public CommandArgument next(Class<?> type) {
//            final int i = nextIndex(type);
//            if (i == -1) throw new NoSuchElementException();
//            this.cursorMap.put(type, i + 1);
//            return get(i);
//        }
//
//        public <T> T getNext(Class<T> type) {
//            final int cursor = this.cursorMap.putIfAbsent(type, 0);
//            for (int i = cursor; i < size(); i++) {
//                CommandArgument arg = get(i);
//                T asType = arg.getAsType(type);
//                if (asType != null) return asType;
//            }
//            return null;
//        }
//
//        public CommandArgument current(Class<?> type) {
//            final int cursor = this.cursorMap.putIfAbsent(type, 0);
//            if (cursor <= 0 || cursor > size()) throw new NoSuchElementException();
//            return get(cursor - 1);
//        }
//
//        public int nextIntegerIndex() {
//            return nextIndex(Integer.TYPE);
//        }
//
//        public int nextLongIndex() {
//            return nextIndex(Long.TYPE);
//        }
//
//        public int nextFloatIndex() {
//            return nextIndex(Float.TYPE);
//        }
//
//        public int nextDoubleIndex() {
//            return nextIndex(Double.TYPE);
//        }
//
//        public int nextRangeIndex() {
//            return nextIndex(IntStream.class);
//        }
//
//        public int nextUserIndex() {
//            return nextIndex(User.class);
//        }
//
//        public int nextMemberIndex() {
//            return nextIndex(Member.class);
//        }
//
//        public int nextRoleIndex() {
//            return nextIndex(Role.class);
//        }
//
//        public int nextTextChannelIndex() {
//            return nextIndex(TextChannel.class);
//        }
//
//        public int nextEmoteIndex() {
//            return nextIndex(Emote.class);
//        }
//
//        public int nextEmojiIndex() {
//            return nextIndex(Emoji.class);
//        }
//
//        public boolean hasNextInteger() {
//            return hasNext(Integer.TYPE);
//        }
//
//        public boolean hasNextLong() {
//            return hasNext(Long.TYPE);
//        }
//
//        public boolean hasNextFloat() {
//            return hasNext(Float.TYPE);
//        }
//
//        public boolean hasNextDouble() {
//            return hasNext(Double.TYPE);
//        }
//
//        public boolean hasNextRange() {
//            return hasNext(IntStream.class);
//        }
//
//        public boolean hasNextUser() {
//            return hasNext(User.class);
//        }
//
//        public boolean hasNextMember() {
//            return hasNext(Member.class);
//        }
//
//        public boolean hasNextRole() {
//            return hasNext(Role.class);
//        }
//
//        public boolean hasNextTextChannel() {
//            return hasNext(TextChannel.class);
//        }
//
//        public boolean hasNextEmote() {
//            return hasNext(Emote.class);
//        }
//
//        public boolean hasNextEmoji() {
//            return hasNext(Emoji.class);
//        }
//
//        public CommandArgument nextInteger() {
//            return next(Integer.TYPE);
//        }
//
//        public CommandArgument nextLong() {
//            return next(Long.TYPE);
//        }
//
//        public CommandArgument nextFloat() {
//            return next(Float.TYPE);
//        }
//
//        public CommandArgument nextDouble() {
//            return next(Double.TYPE);
//        }
//
//        public CommandArgument nextRange() {
//            return next(IntStream.class);
//        }
//
//        public CommandArgument nextUser() {
//            return next(User.class);
//        }
//
//        public CommandArgument nextMember() {
//            return next(Member.class);
//        }
//
//        public CommandArgument nextRole() {
//            return next(Role.class);
//        }
//
//        public CommandArgument nextTextChannel() {
//            return next(TextChannel.class);
//        }
//
//        public CommandArgument nextEmote() {
//            return next(Emote.class);
//        }
//
//        public CommandArgument nextEmoji() {
//            return next(Emoji.class);
//        }
//
//        public CommandArgument currentInteger() {
//            return current(Integer.TYPE);
//        }
//
//        public CommandArgument currentLong() {
//            return current(Long.TYPE);
//        }
//
//        public CommandArgument currentFloat() {
//            return current(Float.TYPE);
//        }
//
//        public CommandArgument currentDouble() {
//            return current(Double.TYPE);
//        }
//
//        public CommandArgument currentRange() {
//            return current(IntStream.class);
//        }
//
//        public CommandArgument currentUser() {
//            return current(User.class);
//        }
//
//        public CommandArgument currentMember() {
//            return current(Member.class);
//        }
//
//        public CommandArgument currentRole() {
//            return current(Role.class);
//        }
//
//        public CommandArgument currentTextChannel() {
//            return current(TextChannel.class);
//        }
//
//        public CommandArgument currentEmote() {
//            return current(Emote.class);
//        }
//
//        public CommandArgument currentEmoji() {
//            return current(Emoji.class);
//        }
//
//    }

}
