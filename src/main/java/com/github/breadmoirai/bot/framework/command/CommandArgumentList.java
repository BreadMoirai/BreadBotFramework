package com.github.breadmoirai.bot.framework.command;

import com.github.breadmoirai.bot.framework.command.parameter.ArgumentTypeMapper;
import com.github.breadmoirai.bot.framework.command.parameter.CommandArgument;
import com.github.breadmoirai.bot.framework.command.parameter.CommandArgumentFactory;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import com.github.breadmoirai.bot.util.Emoji;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.dv8tion.jda.core.entities.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A list implementation for {@link com.github.breadmoirai.bot.framework.command.parameter.CommandArgument CommandArguments} that includes a specialized iterator. All arguments are in lowercase.
 */
public class CommandArgumentList extends AbstractList<CommandArgument> {

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
     * @param types the {@link java.util.Spliterator} will only see {@link com.github.breadmoirai.bot.framework.command.parameter.CommandArgument CommandArguments} that satisfy at least one of the types provided.
     * @return a {@link java.util.Spliterator} of type CommandArgument.
     */
    public Spliterator<CommandArgument> spliterator(Class<?>... types) {
        return Spliterators.spliteratorUnknownSize(typeIterator(types), Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
    }

    /**
     * Attempts to match each {@link com.github.breadmoirai.bot.framework.command.parameter.CommandArgument} to a {@link ArgumentTypeMapper} in the order passed.
     * Will automatically fail if the amount of {@link ArgumentTypeMapper ArgumentTypes} provided is greater than the amount of arguments passed.
     *
     * @param types the types of which to look for.
     * @return {@code true} if the {@link ArgumentTypeMapper ArgumentTypes} provided match the {@link com.github.breadmoirai.bot.framework.command.parameter.CommandArgument CommandArguments} in this list with {@link com.github.breadmoirai.bot.framework.command.parameter.CommandArgument#isOfType(Class)}
     */
    public boolean matchesType(Class<?>... types) {
        return matchesType(0, types);
    }

    /**
     * Attempts to match each {@link com.github.breadmoirai.bot.framework.command.parameter.CommandArgument} to a {@link ArgumentTypeMapper} in the order passed.
     * Will automatically fail if the amount of {@link ArgumentTypeMapper ArgumentTypes} provided is greater than the amount of arguments passed.
     *
     * @param startIndex the starting index of which to start matching Types.
     * @param types      the types of which to look for.
     * @return {@code true} if the {@link ArgumentTypeMapper ArgumentTypes} provided match the {@link com.github.breadmoirai.bot.framework.command.parameter.CommandArgument CommandArguments} in this list with {@link com.github.breadmoirai.bot.framework.command.parameter.CommandArgument#isOfType(Class)}
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
        return stream(Integer.class, IntStream.class).flatMapToInt(CommandArgument::parseRange);
    }

    public Stream<CommandArgument> stream(Class<?>... types) {
        return StreamSupport.stream(spliterator(types), false);
    }

    public ArgumentIterator argumentIterator() {
        return new ArgumentIterator();
    }

    public ArgumentTypeIterator typeIterator(Class<?>... types) {
        return new ArgumentTypeIterator(types);
    }

    public class ArgumentTypeIterator implements Iterator<CommandArgument> {
        private int cursor;
        private final Class<?>[] types;

        private ArgumentTypeIterator(Class<?>[] types) {
            this.types = types;
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to {@link #next}. (Returns {@code -1} if the list
         * iterator is at the end of the list.)
         *
         * @return the index of the element that would be returned by a
         * subsequent call to {@code next}, or {@code -1} if the list
         * iterator is at the end of the list
         */
        public int nextIndex() {
            for (int i = cursor; i < size(); i++) {
                for (Class<?> type : types) {
                    if (get(i).isOfType(type)) {
                        return i;
                    }
                }
            }
            return -1;
        }

        /**
         * Returns {@code true} if this list iterator has more elements when
         * traversing the list in the forward direction. (In other words,
         * returns {@code true} if {@link #next} would return an element rather
         * than throwing an exception.)
         *
         * @return {@code true} if the list iterator has more elements when
         * traversing the list in the forward direction
         */
        public boolean hasNext() {
            return nextIndex() != -1;
        }

        /**
         * Returns the next element in the list and advances the cursor position.
         * This method may be called repeatedly to iterate through the list.
         *
         * @return
         */
        public CommandArgument next() {
            final int i = nextIndex();
            if (i == -1) throw new NoSuchElementException();
            this.cursor = i + 1;
            return get(i);
        }

        public CommandArgument current() {
            if (cursor == 0 || cursor > size()) throw new NoSuchElementException();
            return get(cursor - 1);
        }
    }


    public class ArgumentIterator {

        private final TObjectIntMap<Class<?>> cursorMap;

        private ArgumentIterator() {
            cursorMap = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to {@link #next}. (Returns {@code -1} if the list
         * iterator is at the end of the list.)
         *
         * @return the index of the element that would be returned by a
         * subsequent call to {@code next}, or {@code -1} if the list
         * iterator is at the end of the list
         */
        public int nextIndex(Class<?> type) {
            final int cursor = this.cursorMap.putIfAbsent(type, 0);
            return indexOfType(cursor, type);
        }

        /**
         * Returns {@code true} if this list iterator has more elements when
         * traversing the list in the forward direction. (In other words,
         * returns {@code true} if {@link #next} would return an element rather
         * than throwing an exception.)
         *
         * @param type
         * @return {@code true} if the list iterator has more elements when
         * traversing the list in the forward direction
         */
        public boolean hasNext(Class<?> type) {
            return nextIndex(type) != -1;
        }

        /**
         * Returns the next element in the list and advances the cursor position.
         * This method may be called repeatedly to iterate through the list.
         *
         * @param type
         * @return
         */
        public CommandArgument next(Class<?> type) {
            final int i = nextIndex(type);
            if (i == -1) throw new NoSuchElementException();
            this.cursorMap.put(type, i + 1);
            return get(i);
        }

        public CommandArgument current(Class<?> type) {
            final int cursor = this.cursorMap.putIfAbsent(type, 0);
            if (cursor <= 0 || cursor > size()) throw new NoSuchElementException();
            return get(cursor - 1);
        }

        public int nextIntegerIndex() {
            return nextIndex(Integer.TYPE);
        }

        public int nextLongIndex() {
            return nextIndex(Long.TYPE);
        }

        public int nextFloatIndex() {
            return nextIndex(Float.TYPE);
        }

        public int nextDoubleIndex() {
            return nextIndex(Double.TYPE);
        }

        public int nextRangeIndex() {
            return nextIndex(IntStream.class);
        }

        public int nextUserIndex() {
            return nextIndex(User.class);
        }

        public int nextMemberIndex() {
            return nextIndex(Member.class);
        }

        public int nextRoleIndex() {
            return nextIndex(Role.class);
        }

        public int nextTextChannelIndex() {
            return nextIndex(TextChannel.class);
        }

        public int nextEmoteIndex() {
            return nextIndex(Emote.class);
        }

        public int nextEmojiIndex() {
            return nextIndex(Emoji.class);
        }

        public boolean hasNextInteger() {
            return hasNext(Integer.TYPE);
        }

        public boolean hasNextLong() {
            return hasNext(Long.TYPE);
        }

        public boolean hasNextFloat() {
            return hasNext(Float.TYPE);
        }

        public boolean hasNextDouble() {
            return hasNext(Double.TYPE);
        }

        public boolean hasNextRange() {
            return hasNext(IntStream.class);
        }

        public boolean hasNextUser() {
            return hasNext(User.class);
        }

        public boolean hasNextMember() {
            return hasNext(Member.class);
        }

        public boolean hasNextRole() {
            return hasNext(Role.class);
        }

        public boolean hasNextTextChannel() {
            return hasNext(TextChannel.class);
        }

        public boolean hasNextEmote() {
            return hasNext(Emote.class);
        }

        public boolean hasNextEmoji() {
            return hasNext(Emoji.class);
        }

        public CommandArgument nextInteger() {
            return next(Integer.TYPE);
        }

        public CommandArgument nextLong() {
            return next(Long.TYPE);
        }

        public CommandArgument nextFloat() {
            return next(Float.TYPE);
        }

        public CommandArgument nextDouble() {
            return next(Double.TYPE);
        }

        public CommandArgument nextRange() {
            return next(IntStream.class);
        }

        public CommandArgument nextUser() {
            return next(User.class);
        }

        public CommandArgument nextMember() {
            return next(Member.class);
        }

        public CommandArgument nextRole() {
            return next(Role.class);
        }

        public CommandArgument nextTextChannel() {
            return next(TextChannel.class);
        }

        public CommandArgument nextEmote() {
            return next(Emote.class);
        }

        public CommandArgument nextEmoji() {
            return next(Emoji.class);
        }

        public CommandArgument currentInteger() {
            return current(Integer.TYPE);
        }

        public CommandArgument currentLong() {
            return current(Long.TYPE);
        }

        public CommandArgument currentFloat() {
            return current(Float.TYPE);
        }

        public CommandArgument currentDouble() {
            return current(Double.TYPE);
        }

        public CommandArgument currentRange() {
            return current(IntStream.class);
        }

        public CommandArgument currentUser() {
            return current(User.class);
        }

        public CommandArgument currentMember() {
            return current(Member.class);
        }

        public CommandArgument currentRole() {
            return current(Role.class);
        }

        public CommandArgument currentTextChannel() {
            return current(TextChannel.class);
        }

        public CommandArgument currentEmote() {
            return current(Emote.class);
        }

        public CommandArgument currentEmoji() {
            return current(Emoji.class);
        }

    }
}
