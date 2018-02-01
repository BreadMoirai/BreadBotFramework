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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EventWaiter implements EventListener {

    private final Map<Class<? extends Event>, List<EventAction>> waitingEvents;

    private final ScheduledExecutorService executorService;

    public EventWaiter() {
        this.waitingEvents = new HashMap<>();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public EventWaiter(ScheduledExecutorService executorService) {
        this.waitingEvents = new HashMap<>();
        this.executorService = executorService;
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
        getActions(eventClass).add(action);
    }

    void removeAction(Class<? extends Event> eventClass, EventAction action) {
        getActions(eventClass).remove(action);
    }

    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executorService.schedule(command, delay, unit);
    }

    private <T extends Event> List<EventAction> getActions(Class<T> eventType) {
        final List<EventAction> list = waitingEvents.get(eventType);
        if (list != null) {
            return list;
        }
        final List<EventAction> newList = new ArrayList<>();
        waitingEvents.put(eventType, newList);
        return newList;
    }

    @SubscribeEvent
    @Override
    public final void onEvent(Event event) {
        Class c = event.getClass();
        while (c != Object.class) {
            if (waitingEvents.containsKey(c)) {
                List<EventAction> list = waitingEvents.get(c);
                final List<EventAction> remove = new LinkedList<>();
                for (EventAction eventAction : list) {
                    if (eventAction.accept(event)) {
                        remove.add(eventAction);
                    }
                }
                list.removeAll(remove);
            }
            if (event instanceof ShutdownEvent) {
                executorService.shutdown();
            }
            c = c.getSuperclass();
        }
    }


}