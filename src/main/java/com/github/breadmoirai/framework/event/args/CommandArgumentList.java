package com.github.breadmoirai.framework.event.args;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.AbstractList;
import java.util.Spliterator;
import java.util.Spliterators;

public class CommandArgumentList extends AbstractList<CommandArgument> {

    final private String[] strings;
    final private CommandArgument[] arguments;
    private final JDA jda;
    private final Guild guild;
    private final TextChannel channel;
    private final CommandArgumentFactory factory;

    public CommandArgumentList(String[] strings, JDA jda, Guild guild, TextChannel channel) {
        this.strings = strings;
        arguments = new CommandArgument[strings.length];
        this.jda = jda;
        this.guild = guild;
        this.channel = channel;
        factory = new CommandArgumentFactory(jda, guild, channel);
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
        if (arguments[index] == null) {
            arguments[index] = factory.parse(strings[index]);
        }
        return arguments[index];
    }

    @Override
    public int size() {
        return strings.length;
    }

    @Override
    public Spliterator<CommandArgument> spliterator() {
        return Spliterators.spliterator(this, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
    }
}
