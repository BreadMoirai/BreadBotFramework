package com.github.breadmoirai.breadbot.framework.internal.parameter.argumentimpl;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import net.dv8tion.jda.core.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RoleArgument extends MentionArgument {

    private final Role role;

    public RoleArgument(CommandEvent event, String s, Role role) {
        super(event, s);
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
