/*
 *       Copyright 2016 John Grosh (jagrosh).
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
 *
 *   Modified by Ton Ly (BreadMoirai)
 */
package com.github.breadmoirai.bot.framework.waiter;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>Original class from <a href="https://github.com/JDA-Applications/JDA-Utilities/blob/master/src/main/java/com/jagrosh/jdautilities/waiter/EventWaiter.java">EventWaiter.java</a> in Github Project <a href="https://github.com/JDA-Applications/JDA-Utilities">JDA -Utilities</a> created by John Grosh (jagrosh).
 * Modified by Ton Ly (BreadMoirai)
 *
 * <p>The EventWaiter is capable of handling specialized forms of {@link net.dv8tion.jda.core.events.Event Event}
 * that must meet criteria not normally specifiable without implementation of an {@link net.dv8tion.jda.core.hooks.EventListener EventListener}.
 *
 * <p>This is a singleton class accessed through {@link EventWaiter#get() EventWaiter#get}
 *
 * @author John Grosh (jagrosh)
 */
public class EventWaiter implements EventListener {

    private static final EventWaiter INSTANCE;

    static {
        INSTANCE = new EventWaiter();
    }

    /**
     * Retrieves the singleton instance of an EventWaiter.
     */
    public static EventWaiter get() {
        return INSTANCE;
    }


    private final HashMap<Class<?>, List<Predicate>> waitingEvents;

    private final ScheduledExecutorService threadpool;

    /**
     * Constructs an empty EventWaiter.
     */
    private EventWaiter()
    {
        waitingEvents = new HashMap<>();
        threadpool = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Waits an indefinite amount of time for an {@link net.dv8tion.jda.core.events.Event Event} that
     * returns {@code true} when tested with the provided {@link java.util.function.Predicate Predicate}.
     *
     * <p>When this occurs, the provided {@link java.util.function.Consumer Consumer} will accept and
     * execute using the same Event.
     *
     * @param  <T>
     *         The type of Event to wait for
     * @param  classType
     *         The {@link java.lang.Class} of the Event to wait for
     * @param  action
     *         A Predicate. Should return {@code true} when action is complete, else {@code false} if action is not satisfied and should wait for additional events.
     */
    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> action)
    {
        waitForEvent(classType, action, -1, null, null);
    }

    /**
     * Waits a predetermined amount of time for an {@link net.dv8tion.jda.core.events.Event Event} that
     * returns {@code true} when tested with the provided {@link java.util.function.Predicate Predicate}.
     *
     * <p>Once started, there are two possible outcomes:
     * <ul>
     *     <li>The correct Event occurs within the time alloted, and the provided
     *     {@link java.util.function.Consumer Consumer} will accept and execute using the same Event.</li>
     *
     *     <li>The time limit is elapsed and the provided {@link java.lang.Runnable} is executed.</li>
     * </ul>
     *
     * @param  <T>
     *         The type of Event to wait for
     * @param  eventType
     *         The {@link java.lang.Class} of the Event to wait for
     * @param  action
     *         The Predicate that
     * @param  timeout
     *         The maximum amount of time to wait for
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} measurement of the timeout
     * @param  timeoutAction
     *         The Runnable to run if the time runs out before a correct Event is thrown
     */
    public <T extends Event> void waitForEvent(Class<T> eventType, Predicate<T> action, long timeout, TimeUnit unit, Runnable timeoutAction)
    {
        List<Predicate> list;
        if(waitingEvents.containsKey(eventType))
            list = waitingEvents.get(eventType);
        else {
            list = new ArrayList<>();
            waitingEvents.put(eventType, list);
        }
        list.add(action);
        if(timeout>0 && unit!=null)
            threadpool.schedule(() -> {
                if(list.remove(action) && timeoutAction!=null)
                    timeoutAction.run();
            }, timeout, unit);
    }

    @SubscribeEvent
    @Override
    public final void onEvent(Event event)
    {
        Class c = event.getClass();
        while(c.getSuperclass()!=null) {
            if(waitingEvents.containsKey(c))
            {
                List<Predicate> list = waitingEvents.get(c);
                List<Predicate> ulist = new ArrayList<>(list);
                //noinspection unchecked
                list.removeAll(ulist.stream().filter(i -> i.test(event)).collect(Collectors.toList()));
            }
            if(event instanceof ShutdownEvent)
            {
                threadpool.shutdown();
            }
            c = c.getSuperclass();
        }
    }

}