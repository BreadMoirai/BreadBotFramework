package com.github.breadmoirai.breadbot.plugins.waiter;

import net.dv8tion.jda.core.events.Event;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TimedEventActionImpl<E extends Event, V> extends EventActionImpl<E, V> {

    private final long timeout;
    private final TimeUnit unit;
    private final Runnable timeoutAction;
    private final ScheduledFuture<?> schedule;

    public TimedEventActionImpl(Class<E> eventClass, Predicate<E> condition, Consumer<E> action, ObjectIntPredicate<E> stopper, Function<E, V> finisher, long timeout, TimeUnit unit, Runnable timeoutAction, EventWaiter waiter) {
        super(eventClass, condition, action, stopper, finisher, waiter);
        this.timeout = timeout;
        this.unit = unit;
        this.timeoutAction = timeoutAction;
        schedule = waiter.schedule(this::timeout, timeout, unit);
    }

    private void timeout() {
        if (super.cancel()) {
            timeoutAction.run();
        }
    }

    @Override
    public boolean cancel() {
        schedule.cancel(false);
        return super.cancel();
    }
}
