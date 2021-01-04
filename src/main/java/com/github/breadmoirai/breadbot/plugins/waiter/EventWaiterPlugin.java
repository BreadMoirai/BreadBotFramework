package com.github.breadmoirai.breadbot.plugins.waiter;

import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.concurrent.ScheduledExecutorService;

public class EventWaiterPlugin implements CommandPlugin, EventListener {

    private final EventWaiter eventWaiter;

    public EventWaiterPlugin(ScheduledExecutorService service) {
        this.eventWaiter = new EventWaiter(service);
    }

    public EventWaiterPlugin() {
        eventWaiter = new EventWaiter();
    }

    @Override
    public void initialize(BreadBotBuilder builder) {
        builder.bindTypeModifier(EventWaiter.class, p -> p.setParser((parameter, list, parser) -> eventWaiter));
        builder.bindInjection(EventWaiter.class, eventWaiter);
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            event.getJDA().addEventListener(eventWaiter);
            event.getJDA().removeEventListener(this);
        }
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }
}
