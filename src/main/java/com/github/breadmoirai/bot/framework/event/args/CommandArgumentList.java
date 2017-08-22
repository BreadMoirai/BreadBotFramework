package com.github.breadmoirai.bot.framework.event.args;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;

/**
 * A list implementation for {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument CommandArguments} that includes a specialized iterator. All arguments are in lowercase.
 */
public class CommandArgumentList extends AbstractList<CommandArgument> {

    final private CommandArgument[] arguments;
    private final JDA jda;
    private final Guild guild;
    private final TextChannel channel;

    public CommandArgumentList(String[] strings, JDA jda, Guild guild, TextChannel channel) {
        this.arguments = new CommandArgument[strings.length];
        this.jda = jda;
        this.guild = guild;
        this.channel = channel;
        CommandArgumentFactory factory = new CommandArgumentFactory(jda, guild, channel);
        Arrays.parallelSetAll(arguments, value -> factory.parse(strings[value]));
    }

    public CommandArgumentList(String[] strings, TextChannel channel) {
        this(strings, channel.getJDA(), channel.getGuild(), channel);
    }

    public JDA getJDA() {
        return jda;
    }

    public Guild getGuild() {
        return guild;
    }

    public TextChannel getChannel() {
        return channel;
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
     * Attempts to match each {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument} to a {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType} in the order passed.
     * Will automatically fail if the amount of {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType ArgumentTypes} provided is greater than the amount of arguments passed.
     *
     * @param types the types of which to look for.
     * @return {@code true} if the {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType ArgumentTypes} provided match the {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument CommandArguments} in this list with {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument#isOfType(ArgumentType)}
     */
    public boolean matchesType(ArgumentType... types) {
        return matchesType(0, types);
    }

    /**
     * Attempts to match each {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument} to a {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType} in the order passed.
     * Will automatically fail if the amount of {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType ArgumentTypes} provided is greater than the amount of arguments passed.
     *
     * @param startIndex the starting index of which to start matching Types.
     * @param types      the types of which to look for.
     * @return {@code true} if the {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType ArgumentTypes} provided match the {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument CommandArguments} in this list with {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument#isOfType(ArgumentType)}
     */
    public boolean matchesType(int startIndex, ArgumentType... types) {
        if (startIndex + types.length > size()) return false;
        for (int j = 0; j < types.length; j++) {
            if (!get(j + startIndex).isOfType(types[j])) return false;
        }
        return true;
    }

    /**
     * Finds the index of the first argument that matches the specified type.
     *
     * @param type The {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType} to search for.
     * @return The index of the argument if found. If none of the arguments match the type provided, {@code -1} is returned.
     */
    public int indexOfType(ArgumentType type) {
        return indexOfType(0, type);
    }

    /**
     * Finds the first argument of the specified type that occurs after the index given.
     *
     * @param startIndex The index from which to start searching.
     * @param type       The {@link com.github.breadmoirai.bot.framework.event.args.ArgumentType} to search for.
     * @return The index of the argument if found. If none of the arguments match the type provided, {@code -1} is returned. If the {@code startIndex} provided is less than {@code 0}, it will be treated as {@code 0}. If the {@code startIndex} provided is greater than or equal to the size of this list, {@code -1} will be returned.
     */
    public int indexOfType(int startIndex, ArgumentType type) {
        for (int i = startIndex; i < size(); i++) {
            if (get(i).isOfType(type)) return i;
        }
        return -1;
    }

    public ArgumentIterator argumentIterator() {
        return new ArgumentIterator();
    }

    public ArgumentTypeIterator typeIterator(ArgumentType... types) {
        return new ArgumentTypeIterator(types);
    }

    private class ArgumentTypeIterator {
        private int cursor;
        private final ArgumentType[] types;

        public ArgumentTypeIterator(ArgumentType[] types) {
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
                for (ArgumentType type : types) {
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


    private class ArgumentIterator {

        private final int[] cursor;

        ArgumentIterator() {
            cursor = new int[ArgumentType.values().length];
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to {@link #next}. (Returns {@code -1} if the list
         * iterator is at the end of the list.)
         *
         * @param type
         * @return the index of the element that would be returned by a
         * subsequent call to {@code next}, or {@code -1} if the list
         * iterator is at the end of the list
         */
        public int nextIndex(ArgumentType type) {
            final int cursor = this.cursor[type.ordinal()];
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
        public boolean hasNext(ArgumentType type) {
            return nextIndex(type) != -1;
        }

        /**
         * Returns the next element in the list and advances the cursor position.
         * This method may be called repeatedly to iterate through the list.
         *
         * @param type
         * @return
         */
        public CommandArgument next(ArgumentType type) {
            final int i = nextIndex(type);
            if (i == -1) throw new NoSuchElementException();
            this.cursor[type.ordinal()] = i + 1;
            return get(i);
        }

        public CommandArgument current(ArgumentType type) {
            final int cursor = this.cursor[type.ordinal()];
            if (cursor <= 0 || cursor > size()) throw new NoSuchElementException();
            return get(cursor - 1);
        }

        public int nextIntegerIndex() {
            return nextIndex(ArgumentType.INTEGER);
        }

        public int nextLongIndex() {
            return nextIndex(ArgumentType.LONG);
        }

        public int nextFloatIndex() {
            return nextIndex(ArgumentType.FLOAT);
        }

        public int nextDoubleIndex() {
            return nextIndex(ArgumentType.DOUBLE);
        }

        public int nextRangeIndex() {
            return nextIndex(ArgumentType.RANGE);
        }

        public int nextHexIndex() {
            return nextIndex(ArgumentType.HEX);
        }

        public int nextUserIndex() {
            return nextIndex(ArgumentType.USER);
        }

        public int nextMemberIndex() {
            return nextIndex(ArgumentType.MEMBER);
        }

        public int nextRoleIndex() {
            return nextIndex(ArgumentType.ROLE);
        }

        public int nextTextChannelIndex() {
            return nextIndex(ArgumentType.TEXTCHANNEL);
        }

        public int nextEmoteIndex() {
            return nextIndex(ArgumentType.EMOTE);
        }

        public int nextEmojiIndex() {
            return nextIndex(ArgumentType.EMOJI);
        }

        public boolean hasNextInteger() {
            return hasNext(ArgumentType.INTEGER);
        }

        public boolean hasNextLong() {
            return hasNext(ArgumentType.LONG);
        }

        public boolean hasNextFloat() {
            return hasNext(ArgumentType.FLOAT);
        }

        public boolean hasNextDouble() {
            return hasNext(ArgumentType.DOUBLE);
        }

        public boolean hasNextRange() {
            return hasNext(ArgumentType.RANGE);
        }

        public boolean hasNextHex() {
            return hasNext(ArgumentType.HEX);
        }

        public boolean hasNextUser() {
            return hasNext(ArgumentType.USER);
        }

        public boolean hasNextMember() {
            return hasNext(ArgumentType.MEMBER);
        }

        public boolean hasNextRole() {
            return hasNext(ArgumentType.ROLE);
        }

        public boolean hasNextTextChannel() {
            return hasNext(ArgumentType.TEXTCHANNEL);
        }

        public boolean hasNextEmote() {
            return hasNext(ArgumentType.EMOTE);
        }

        public boolean hasNextEmoji() {
            return hasNext(ArgumentType.EMOJI);
        }

        public CommandArgument nextInteger() {
            return next(ArgumentType.INTEGER);
        }

        public CommandArgument nextLong() {
            return next(ArgumentType.LONG);
        }

        public CommandArgument nextFloat() {
            return next(ArgumentType.FLOAT);
        }

        public CommandArgument nextDouble() {
            return next(ArgumentType.DOUBLE);
        }

        public CommandArgument nextRange() {
            return next(ArgumentType.RANGE);
        }

        public CommandArgument nextHex() {
            return next(ArgumentType.HEX);
        }

        public CommandArgument nextUser() {
            return next(ArgumentType.USER);
        }

        public CommandArgument nextMember() {
            return next(ArgumentType.MEMBER);
        }

        public CommandArgument nextRole() {
            return next(ArgumentType.ROLE);
        }

        public CommandArgument nextTextChannel() {
            return next(ArgumentType.TEXTCHANNEL);
        }

        public CommandArgument nextEmote() {
            return next(ArgumentType.EMOTE);
        }

        public CommandArgument nextEmoji() {
            return next(ArgumentType.EMOJI);
        }

        public CommandArgument currentInteger() {
            return current(ArgumentType.INTEGER);
        }

        public CommandArgument currentLong() {
            return current(ArgumentType.LONG);
        }

        public CommandArgument currentFloat() {
            return current(ArgumentType.FLOAT);
        }

        public CommandArgument currentDouble() {
            return current(ArgumentType.DOUBLE);
        }

        public CommandArgument currentRange() {
            return current(ArgumentType.RANGE);
        }

        public CommandArgument currentHex() {
            return current(ArgumentType.HEX);
        }

        public CommandArgument currentUser() {
            return current(ArgumentType.USER);
        }

        public CommandArgument currentMember() {
            return current(ArgumentType.MEMBER);
        }

        public CommandArgument currentRole() {
            return current(ArgumentType.ROLE);
        }

        public CommandArgument currentTextChannel() {
            return current(ArgumentType.TEXTCHANNEL);
        }

        public CommandArgument currentEmote() {
            return current(ArgumentType.EMOTE);
        }

        public CommandArgument currentEmoji() {
            return current(ArgumentType.EMOJI);
        }

    }
}
