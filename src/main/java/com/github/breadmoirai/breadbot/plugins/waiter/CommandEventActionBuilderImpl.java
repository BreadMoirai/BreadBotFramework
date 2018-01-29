package com.github.breadmoirai.breadbot.plugins.waiter;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CommandEventActionBuilderImpl<T> implements CommandEventActionBuilder<T> {

    private final EventActionBuilderImpl<CommandEvent, T> actionBuilder;

    public CommandEventActionBuilderImpl(EventWaiter eventWaiter) {
        actionBuilder = new EventActionBuilderImpl<>(CommandEvent.class, eventWaiter);
    }

    @Override
    public CommandEventActionBuilder<T> matching(Predicate<CommandEvent> condition) {
        final Predicate<CommandEvent> builderCondition = actionBuilder.getCondition();
        if (builderCondition == null) {
            actionBuilder.setCondition(condition);
        } else {
            actionBuilder.setCondition(builderCondition.and(condition));
        }
        return this;
    }

    @Override
    public EventActionBuilder<CommandEvent, T> condition(Predicate<CommandEvent> condition) {
        return actionBuilder.condition(condition);
    }

    @Override
    public EventActionBuilder<CommandEvent, T> action(Consumer<CommandEvent> action) {
        return actionBuilder.action(action);
    }

    @Override
    public EventActionBuilder<CommandEvent, T> stopIf(ObjectIntPredicate<CommandEvent> stopper) {
        return actionBuilder.stopIf(stopper);
    }

    @Override
    public <R> EventActionBuilder<CommandEvent, R> finishWithResult(Function<CommandEvent, R> finisher) {
        return actionBuilder.finishWithResult(finisher);
    }

    @Override
    public EventActionBuilder<CommandEvent, T> waitFor(long timeout, TimeUnit unit) {
        return actionBuilder.waitFor(timeout, unit);
    }

    @Override
    public EventActionBuilder<CommandEvent, T> timeout(Runnable timeoutAction) {
        return actionBuilder.timeout(timeoutAction);
    }

    @Override
    public EventActionFuture<T> build() {
        return actionBuilder.build();
    }
}
