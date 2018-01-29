package com.github.breadmoirai.breadbot.plugins.waiter;


import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

interface EventActionFuture<V> extends ScheduledFuture<V> {

    CompletionStage<V> toCompletionStage();

    @Override
    default int compareTo(@Nonnull Delayed o) {
        return (int) (o.getDelay(TimeUnit.MILLISECONDS) - this.getDelay(TimeUnit.MILLISECONDS));
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when {@code cancel} is called,
     * this task should never run.  If the task has already started,
     * then the task cannot be interrupted. If there is a timeout action
     * present, it will also be cancelled.
     * <p>
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return {@code true}.  Subsequent calls to {@link #isCancelled}
     * will always return {@code true} if this method returned {@code true}.
     *
     * @param mayInterruptIfRunning has no effect.
     * @return {@code false} if the task could not be cancelled,
     * typically because it has already completed normally;
     * {@code true} otherwise
     */
    @Override
    default boolean cancel(boolean mayInterruptIfRunning) {
        return cancel();
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when {@code cancel} is called,
     * this task should never run.  If the task has already started,
     * then the task cannot be interrupted.
     * <p>
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return {@code true}.  Subsequent calls to {@link #isCancelled}
     * will always return {@code true} if this method returned {@code true}.
     *
     * @return {@code false} if the task could not be cancelled,
     * typically because it has already completed normally;
     * {@code true} otherwise
     */
    boolean cancel();
}
