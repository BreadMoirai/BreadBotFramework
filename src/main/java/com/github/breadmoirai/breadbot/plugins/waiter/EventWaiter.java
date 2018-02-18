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
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EventWaiter implements EventListener {

    private final Map<Class<? extends Event>, Set<EventAction>> waitingEvents;
    private final ScheduledExecutorService executorService;
    private final boolean myService;

    public EventWaiter() {
        this.waitingEvents = new HashMap<>();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        myService = true;
    }

    public EventWaiter(ScheduledExecutorService executorService) {
        this.waitingEvents = new HashMap<>();
        this.executorService = executorService;
        myService = false;
    }

    public <T extends Event> EventActionBuilder<T, Void> waitFor(Class<T> eventClass) {
        return new EventActionBuilderImpl<>(eventClass, this);
    }

    public CommandEventActionBuilder<Void> waitForCommand() {
        return new CommandEventActionBuilderImpl<>(this);
    }

    public ReactionEventActionBuilder<Void> waitForReaction() {
        return new ReactionEventActionBuilderImpl<>(this);
    }

    void addAction(Class<? extends Event> eventClass, EventAction action) {
        waitingEvents.computeIfAbsent(eventClass, e -> new HashSet<>()).add(action);
    }

    void removeAction(Class<? extends Event> eventClass, EventAction action) {
        waitingEvents.computeIfAbsent(eventClass, e -> new HashSet<>()).remove(action);
    }

    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executorService.schedule(command, delay, unit);
    }

    @SubscribeEvent
    @Override
    public final void onEvent(Event event) {
        Class c = event.getClass();
        while (c != Object.class) {
            if (waitingEvents.containsKey(c)) {
                Set<EventAction> list = waitingEvents.get(c);
                if (list != null) {
                    final EventAction[] arr = list.toArray(new EventAction[0]);
                    list.removeAll(Arrays.stream(arr).filter(eventAction -> eventAction.accept(event)).collect(
                            Collectors.toSet()));
                }
            }
            if (event instanceof ShutdownEvent && myService) {
                executorService.shutdownNow();
            }
            c = c.getSuperclass();
        }
    }

}