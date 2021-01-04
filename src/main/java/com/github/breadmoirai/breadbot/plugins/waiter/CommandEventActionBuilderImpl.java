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

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CommandEventActionBuilderImpl<T> implements CommandEventActionBuilder<T> {

    private final EventActionBuilderImpl<CommandEvent, T> actionBuilder;

    private Predicate<CommandEvent> conditionExtension;

    public CommandEventActionBuilderImpl(EventWaiter eventWaiter) {
        actionBuilder = new EventActionBuilderImpl<>(CommandEvent.class, eventWaiter);
    }


    private CommandEventActionBuilderImpl(EventActionBuilderImpl<CommandEvent, T> actionBuilder) {
        this.actionBuilder = actionBuilder;
    }

    @Override
    public CommandEventActionBuilder<T> matching(Predicate<CommandEvent> condition) {
        if (conditionExtension == null) {
            conditionExtension = condition;
        } else {
            conditionExtension = conditionExtension.and(condition);
        }
        return this;
    }

    @Override
    public CommandEventActionBuilder<T> condition(Predicate<CommandEvent> condition) {
        actionBuilder.condition(condition);
        return this;
    }

    @Override
    public CommandEventActionBuilder<T> action(Consumer<CommandEvent> action) {
        actionBuilder.action(action);
        return this;
    }

    @Override
    public CommandEventActionBuilder<T> stopIf(ObjectIntPredicate<CommandEvent> stopper) {
        actionBuilder.stopIf(stopper);
        return this;
    }

    @Override
    public <R> CommandEventActionBuilder<R> finishWithResult(Function<CommandEvent, R> finisher) {
        final EventActionBuilderImpl<CommandEvent, R> result = actionBuilder.cloneWithFinisher(finisher);
        final CommandEventActionBuilderImpl<R> r2 = new CommandEventActionBuilderImpl<>(result);
        r2.conditionExtension = this.conditionExtension;
        return r2;
    }

    @Override
    public CommandEventActionBuilder<T> waitFor(long timeout, TimeUnit unit) {
        actionBuilder.waitFor(timeout, unit);
        return this;
    }

    @Override
    public CommandEventActionBuilder<T> timeout(Runnable timeoutAction) {
        actionBuilder.timeout(timeoutAction);
        return this;
    }

    @Override
    public EventActionFuture<T> build() {
        actionBuilder.setCondition(conditionExtension.and(actionBuilder.getCondition()));
        return actionBuilder.build();
    }
}
