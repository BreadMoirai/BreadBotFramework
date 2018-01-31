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
import net.dv8tion.jda.core.utils.Checks;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EventActionBuilderImpl<E extends Event, V> implements EventActionBuilder<E, V> {

    private final Class<E> eventClass;
    private final EventWaiter eventWaiter;

    private Predicate<E> condition = o -> true;
    private Consumer<E> action;
    private ObjectIntPredicate<E> stopper = (e, i) -> true;
    private Function<E, V> finisher = e -> null;
    private long timeout;
    private TimeUnit unit;
    private Runnable timeoutAction;

    public EventActionBuilderImpl(Class<E> eventClass, EventWaiter eventWaiter) {
        this.eventClass = eventClass;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public EventActionBuilder<E, V> condition(Predicate<E> condition) {
        Checks.notNull(condition, "condition");
        this.condition = condition;
        return this;
    }

    @Override
    public EventActionBuilder<E, V> action(Consumer<E> action) {
        Checks.notNull(action, "action");
        this.action = action;
        return this;
    }

    @Override
    public EventActionBuilder<E, V> stopIf(ObjectIntPredicate<E> stopper) {
        Checks.notNull(stopper, "stopper");
        this.stopper = stopper;
        return this;
    }

    @Override
    public <R> EventActionBuilder<E, R> finishWithResult(Function<E, R> finisher) {
        Checks.notNull(finisher, "finisher");
        return cloneWithFinisher(finisher);
    }

    @Override
    public EventActionBuilder<E, V> waitFor(long timeout, TimeUnit unit) {
        Checks.notNull(unit, "unit");
        Checks.positive(timeout, "timeout");
        this.timeout = timeout;
        this.unit = unit;
        return this;
    }

    @Override
    public EventActionBuilder<E, V> timeout(Runnable timeoutAction) {
        Checks.notNull(timeoutAction, "timeoutAction");
        this.timeoutAction = timeoutAction;
        return this;
    }

    @Override
    public EventActionFuture<V> build() {
        final EventActionImpl<E, V> eventAction = new EventActionImpl<>(eventClass, condition, action, stopper, finisher, eventWaiter);
        eventWaiter.addAction(eventClass, eventAction);
        final EventActionFuture<V> future = eventAction.getFuture();
        if (unit != null) {
            eventWaiter.schedule(() -> {
                if (future.cancel() && timeoutAction != null) {
                    timeoutAction.run();
                }

            }, timeout, unit);
        }
        return future;
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public Predicate<E> getCondition() {
        return condition;
    }

    public void setCondition(Predicate<E> condition) {
        this.condition = condition;
    }

    public Consumer<E> getAction() {
        return action;
    }

    public void setAction(Consumer<E> action) {
        this.action = action;
    }

    public ObjectIntPredicate<E> getStopper() {
        return stopper;
    }

    public void setStopper(ObjectIntPredicate<E> stopper) {
        this.stopper = stopper;
    }

    public Function<E, V> getFinisher() {
        return finisher;
    }

    public void setFinisher(Function<E, V> finisher) {
        this.finisher = finisher;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public Runnable getTimeoutAction() {
        return timeoutAction;
    }

    public void setTimeoutAction(Runnable timeoutAction) {
        this.timeoutAction = timeoutAction;
    }

    <R> EventActionBuilderImpl<E, R> cloneWithFinisher(Function<E, R> finisher) {
        final EventActionBuilderImpl<E, R> c = new EventActionBuilderImpl<>(eventClass, eventWaiter);
        c.setCondition(condition);
        c.setAction(action);
        c.setStopper(stopper);
        c.setFinisher(finisher);
        c.setTimeout(timeout);
        c.setUnit(unit);
        c.setTimeoutAction(timeoutAction);
        return c;
    }
}
