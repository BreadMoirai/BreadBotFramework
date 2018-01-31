package com.github.breadmoirai.breadbot.plugins.waiter;

import net.dv8tion.jda.core.events.Event;

interface EventAction<T extends Event, V> {

    boolean accept(Event event);

    EventActionFuture<V> getFuture();

    boolean cancel();
}
