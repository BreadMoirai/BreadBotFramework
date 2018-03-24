package com.github.breadmoirai.breadbot.framework.response.menu;

import com.github.breadmoirai.breadbot.framework.response.DynamicCommandResponse;
import com.github.breadmoirai.breadbot.plugins.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

public class ReactionMenu implements DynamicCommandResponse {

    private final EventWaiter waiter;
    private Supplier<Message> message;
    private List<MenuReaction> reactions;

    public ReactionMenu(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void dispatch(LongConsumer linkReceiver) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean matches(DynamicCommandResponse other) {
        if (other == null)
            return false;
        final ReactionMenu otherMenu = (ReactionMenu) other;
        return true;
    }

    private static abstract class MenuReaction implements Consumer<Message> {

        @Override
        public void accept(Message message) {
            reactTo(message);
        }

        abstract void onReaction(GenericMessageReactionEvent event);

        abstract void reactTo(Message message);

    }
}
