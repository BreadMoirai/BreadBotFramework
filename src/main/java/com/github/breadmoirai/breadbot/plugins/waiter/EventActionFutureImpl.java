/*
 *     Copyright 2017-2018 Ton Ly
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

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EventActionFutureImpl<V> implements CompletableEventActionFuture<V> {

    private final EventActionImpl<? extends Event, V> action;
    private final CompletableFuture<V> completableFuture;

    public <E extends Event> EventActionFutureImpl(EventActionImpl<E, V> action) {
        this.action = action;
        completableFuture = new CompletableFuture<>();
    }

    @Override
    public long getDelay(@Nonnull TimeUnit unit) {
        return 0;
    }

    @Override
    public boolean cancel() {
        completableFuture.cancel(false);
        return action.cancel();
    }

    @Override
    public boolean isCancelled() {
        return completableFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return completableFuture.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return completableFuture.get();
    }

    @Override
    public V get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(timeout, unit);
    }

    @Override
    public boolean complete(V value) {
        return completableFuture.complete(value);
    }

    @Override
    public CompletionStage<V> toCompletionStage() {
        return completableFuture;
    }
}
