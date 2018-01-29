package com.github.breadmoirai.breadbot.plugins.waiter;

import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class EventWaiterPlugin implements CommandPlugin, EventListener {

    private final EventWaiter eventWaiter;

    public EventWaiterPlugin() {
        eventWaiter = new EventWaiter();
    }

    @Override
    public void initialize(BreadBotBuilder builder) {
        builder.bindTypeModifier(EventWaiter.class, p -> p.setParser((parameter, list, parser) -> eventWaiter));
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ReadyEvent) {
            event.getJDA().addEventListener(eventWaiter);
            event.getJDA().removeEventListener(this);
        }
    }
}
