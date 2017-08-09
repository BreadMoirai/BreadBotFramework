package com.github.breadmoirai.framework.event.args;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class CommandArgumentFactory {
    private final JDA jda;
    private final Guild guild;
    private final TextChannel channel;

    public CommandArgumentFactory(JDA jda, Guild guild, TextChannel channel) {
        this.jda = jda;
        this.guild = guild;
        this.channel = channel;
    }

    public CommandArgument parse(String string) {

    }
}
