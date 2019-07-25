/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.breadmoirai.breadbot.plugins.waiter;

import net.dv8tion.jda.core.events.Event;

import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EventActionImpl<E extends Event, V> implements EventAction<E, V> {
    private final Class<E> eventClass;
    private final Predicate<E> condition;
    private final Consumer<E> action;
    private final ObjectIntPredicate<E> stopper;
    private final Function<E, V> finisher;
    protected final EventActionFutureImpl<V> future;
    protected final EventWaiter waiter;
    private ScheduledFuture<?> timeout;

    private volatile boolean isWaiting = true;
    private boolean running = false;
    private int runCount = 0;

    public EventActionImpl(Class<E> eventClass, Predicate<E> condition, Consumer<E> action, ObjectIntPredicate<E> stopper, Function<E, V> finisher, EventWaiter waiter) {
        this.eventClass = eventClass;
        this.condition = condition;
        this.action = action;
        this.stopper = stopper;
        this.finisher = finisher;
        this.waiter = waiter;
        this.future = new EventActionFutureImpl<>(this);
    }

    @Override
    public boolean accept(Event event) {
        if (!isWaiting) return true;
        @SuppressWarnings("unchecked") final E e = (E) event;
        if (condition.test(e)) {
            if (!isWaiting) return true;
            running = true;
            if (action != null) {
                action.accept(e);
            }
            runCount++;
            if (stopper.test(e, runCount)) {
                isWaiting = false;
                final V result = finisher.apply(e);
                future.complete(result);
                return true;
            }
            running = false;
        }
        return false;
    }

    @Override
    public EventActionFuture<V> getFuture() {
        return future;
    }

    @Override
    public boolean cancel() {
        isWaiting = false;
        if (timeout != null) {
            timeout.cancel(false);
        }
        waiter.removeAction(eventClass, this);
        return !running;
    }

    public void setTimeout(ScheduledFuture<?> timeout) {
        this.timeout = timeout;
    }
}
