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

import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class ReactionEventActionBuilderImpl<T> implements ReactionEventActionBuilder<T> {

    private final EventActionBuilderImpl<GenericMessageReactionEvent, T> actionBuilder;

    private Predicate<GenericMessageReactionEvent> conditionExtension;
    private ObjectIntPredicate<GenericMessageReactionEvent> stopIfExtension;

    public ReactionEventActionBuilderImpl(EventWaiter eventWaiter) {
        actionBuilder = new EventActionBuilderImpl<>(GenericMessageReactionEvent.class, eventWaiter);
    }


    private ReactionEventActionBuilderImpl(EventActionBuilderImpl<GenericMessageReactionEvent, T> actionBuilder) {
        this.actionBuilder = actionBuilder;
    }

    @Override
    public ReactionEventActionBuilder<T> stopOnReactionCount(IntPredicate reactionCount) {
        if (stopIfExtension == null) {
            stopIfExtension = (e, i) -> reactionCount.test(i);
        } else {
            stopIfExtension = (e, i) -> stopIfExtension.test(e, i) && reactionCount.test(i);
        }
        return this;
    }

    @Override
    public ReactionEventActionBuilder<T> matching(Predicate<GenericMessageReactionEvent> condition) {
        if (conditionExtension == null) {
            conditionExtension = condition;
        } else {
            conditionExtension = conditionExtension.and(condition);
        }
        return this;
    }

    @Override
    public ReactionEventActionBuilder<T> condition(Predicate<GenericMessageReactionEvent> condition) {
        actionBuilder.condition(condition);
        return this;
    }

    @Override
    public ReactionEventActionBuilder<T> action(Consumer<GenericMessageReactionEvent> action) {
        actionBuilder.action(action);
        return this;
    }

    @Override
    public ReactionEventActionBuilder<T> stopIf(ObjectIntPredicate<GenericMessageReactionEvent> stopper) {
        actionBuilder.stopIf(stopper);
        return this;
    }

    @Override
    public <R> ReactionEventActionBuilder<R> finishWithResult(Function<GenericMessageReactionEvent, R> finisher) {
        final EventActionBuilderImpl<GenericMessageReactionEvent, R> result = actionBuilder.cloneWithFinisher(finisher);
        final ReactionEventActionBuilderImpl<R> r2 = new ReactionEventActionBuilderImpl<>(result);
        r2.conditionExtension = this.conditionExtension;
        return r2;
    }

    @Override
    public ReactionEventActionBuilder<T> waitFor(long timeout, TimeUnit unit) {
        actionBuilder.waitFor(timeout, unit);
        return this;
    }

    @Override
    public ReactionEventActionBuilder<T> timeout(Runnable timeoutAction) {
        actionBuilder.timeout(timeoutAction);
        return this;
    }

    @Override
    public EventActionFuture<T> build() {
        actionBuilder.setCondition(conditionExtension.and(actionBuilder.getCondition()));
        return actionBuilder.build();
    }

}
