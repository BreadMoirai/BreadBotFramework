/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
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
    private ObjectIntPredicate<E> stopper;
    private Function<E, V> finisher = e -> null;
    private long timeout;
    private TimeUnit unit;
    private Runnable timeoutAction = () -> {
    };

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
        if (unit == null) {
            final EventActionImpl<E, V> eventAction = new EventActionImpl<E, V>(eventClass, condition, action, stopper, finisher, eventWaiter);
            eventWaiter.addAction(eventClass, eventAction);
            return eventAction.getFuture();
        } else {

        }
        return null;
    }

    private <R> EventActionBuilderImpl<E, R> cloneWithFinisher(Function<E, R> finisher) {
        final EventActionBuilderImpl<E, R> c = new EventActionBuilderImpl<>(eventClass, eventWaiter);
        c.condition = this.condition;
        c.action = this.action;
        c.stopper = this.stopper;
        c.finisher = finisher;
        c.timeout = this.timeout;
        c.unit = this.unit;
        c.timeoutAction = this.timeoutAction;
        return c;
    }
}
