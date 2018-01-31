package com.github.breadmoirai.breadbot.plugins.owner;

import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import net.dv8tion.jda.core.entities.Member;

public abstract class OwnerPlugin implements CommandPlugin {

    public abstract boolean isOwner(Member member);

    @Override
    public final void initialize(BreadBotBuilder builder) {
        builder.bindPreprocessorPredicate("owner", Owner.class, event -> isOwner(event.getMember()));
    }


}
