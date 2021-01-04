package com.github.breadmoirai.breadbot.plugins.waiter;

import net.dv8tion.jda.api.events.GenericEvent;

interface EventAction<T extends GenericEvent, V> {

    boolean accept(T event);

    EventActionFuture<V> getFuture();

    boolean cancel();
}
