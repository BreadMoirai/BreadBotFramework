package com.github.breadmoirai.bot.framework.command.arg.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RoleArgument extends MentionArgument {

    private final Role role;

    public RoleArgument(JDA jda, Guild guild, TextChannel channel, String arg, Role role) {
        super(jda, guild, channel, arg);
        this.role = role;
    }

    @Override
    public boolean isRole() {
        return true;
    }

    @Override
    public boolean isValidRole() {
        return true;
    }

    @NotNull
    @Override
    public Role getRole() {
        return role;
    }

    @NotNull
    @Override
    public Optional<Role> findRole() {
        return Optional.of(role);
    }

    @NotNull
    @Override
    public List<Role> searchRoles() {
        return Collections.singletonList(role);
    }
}
