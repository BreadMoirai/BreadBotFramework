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

import net.dv8tion.jda.api.events.Event;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A structure that allows one to wait for events.
 * The basic control flow is
 * <pre><code>
 *  onEvent(Event e)
 *      if condition(e):
 *          action(e)
 *          timesRan++
 *          if stopper(e, timesRan):
 *              stopListening()
 *              return finisher(e)
 *      //otherwise continue listening
 * </code></pre>
 *
 * @param <E>
 *         The Event Type
 * @param <V>
 *         The Result Type
 */
public interface EventActionBuilder<E extends Event, V> {

    /**
     * Sets the condition for accepting events.
     * If this value is not set, all events will be accepted.
     *
     * @param condition
     *         a predicate that tests Events, returning {@code true} if it should be passed to the action.
     *
     * @return this
     */
    EventActionBuilder<E, V> condition(Predicate<E> condition);

    /**
     * Sets the action to perform on the event after the condition tests it.
     *
     * @param action
     *         a Consumer that accepts the tested event.
     *
     * @return this
     */
    EventActionBuilder<E, V> action(Consumer<E> action);

    /**
     * Sets a Predicate that takes in the Event and the number of times the action has run.
     * This is run right after the action is called.
     * By default, this field is set with {@code (e, i) -> true}.
     *
     * @param stopper
     *         a predicate that takes in an {@code Event} and an {@code int} representing the number of times this action has run,
     *         returning {@code true} if this should stop listening for more events, false if it should continue to listen for events.
     *
     * @return this
     */
    EventActionBuilder<E, V> stopIf(ObjectIntPredicate<E> stopper);

    /**
     * Sets a Function that runs after the stopper is called with the Event as the parameter, producing an Object.
     * This allows the {@link EventActionFuture} to return a Result when it finishes waiting for events.
     * <p>By default, the finisher produces {@code null} as a result.
     *
     * @param finisher
     *         A Function that takes in an Event and produces a result which can be obtained through the Future,
     * @param <V2>
     *         the new result Type
     *
     * @return a new builder with modified generic parameters, retaining all set fields.
     */
    <V2> EventActionBuilder<E, V2> finishWithResult(Function<E, V2> finisher);

    /**
     * <b>WARNING THIS RETURNS A NEW BUILDER</b>
     *
     * @param finisher a runnable that runs when this action is complete
     *
     * @return a new builder
     */
    default EventActionBuilder<E, Void> finish(Runnable finisher) {
        return finishWithResult(e -> {
            finisher.run();
            return null;
        });
    }

    EventActionBuilder<E, V> waitFor(long timeout, TimeUnit unit);

    EventActionBuilder<E, V> timeout(Runnable timeoutAction);

    EventActionFuture<V> build();

}
