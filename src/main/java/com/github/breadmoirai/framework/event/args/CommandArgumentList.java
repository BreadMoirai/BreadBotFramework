package com.github.breadmoirai.framework.event.args;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;

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
     * Attempts to match each {@link com.github.breadmoirai.framework.event.args.CommandArgument} to a {@link com.github.breadmoirai.framework.event.args.ArgumentType} in the order passed.
     * Will automatically fail if the amount of {@link com.github.breadmoirai.framework.event.args.ArgumentType ArgumentTypes} provided is greater than the amount of arguments passed.
     *
     * @param types the types of which to look for.
     *
     * @return {@code true} if the {@link com.github.breadmoirai.framework.event.args.ArgumentType ArgumentTypes} provided match the {@link com.github.breadmoirai.framework.event.args.CommandArgument CommandArguments} in this list with {@link com.github.breadmoirai.framework.event.args.CommandArgument#isOfType(ArgumentType)}
     */
    public boolean matchesType(ArgumentType... types) {
        return matchesType(0, types);
    }

    /**
     * Attempts to match each {@link com.github.breadmoirai.framework.event.args.CommandArgument} to a {@link com.github.breadmoirai.framework.event.args.ArgumentType} in the order passed.
     * Will automatically fail if the amount of {@link com.github.breadmoirai.framework.event.args.ArgumentType ArgumentTypes} provided is greater than the amount of arguments passed.
     *
     * @param startIndex the starting index of which to start matching Types.
     * @param types the types of which to look for.
     *
     * @return {@code true} if the {@link com.github.breadmoirai.framework.event.args.ArgumentType ArgumentTypes} provided match the {@link com.github.breadmoirai.framework.event.args.CommandArgument CommandArguments} in this list with {@link com.github.breadmoirai.framework.event.args.CommandArgument#isOfType(ArgumentType)}
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
     * @param type The {@link com.github.breadmoirai.framework.event.args.ArgumentType} to search for.
     *
     * @return The index of the argument if found. If none of the arguments match the type provided, {@code -1} is returned.
     */
    public int indexOf(ArgumentType type) {
        return indexOf(0, type);
    }

    /**
     * Finds the first argument of the specified type that occurs after the index given.
     *
     * @param startIndex The index from which to start searching.
     * @param type The {@link com.github.breadmoirai.framework.event.args.ArgumentType} to search for.
     *
     * @return The index of the argument if found. If none of the arguments match the type provided, {@code -1} is returned. If the {@code startIndex} provided is less than {@code 0}, it will be treated as {@code 0}. If the {@code startIndex} provided is greater than or equal to the size of this list, {@code -1} will be returned.
     */
    public int indexOf(int startIndex, ArgumentType type) {
        for (int i = startIndex; i < size(); i++) {
            if (get(i).isOfType(type)) return i;
        }
        return -1;
    }
}
