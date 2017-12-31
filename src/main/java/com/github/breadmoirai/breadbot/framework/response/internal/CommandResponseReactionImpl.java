/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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
package com.github.breadmoirai.breadbot.framework.response.internal;

import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import com.github.breadmoirai.breadbot.framework.response.RestActionExtension;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.Checks;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

public class CommandResponseReactionImpl extends CommandResponse implements RestActionExtension<Void> {

    private final Supplier<RestAction<Void>> restActionSupplier;
    private long delay;
    private TimeUnit unit;
    private Consumer<Void> success;
    private Consumer<Throwable> failure;

    public CommandResponseReactionImpl(Supplier<RestAction<Void>> restActionSupplier) {
        this.restActionSupplier = restActionSupplier;
    }

    @Override
    public void dispatch(LongConsumer linkReceiver) {
        if (delay > 0) {
            restActionSupplier.get().queueAfter(delay, unit, success, failure);
        } else {
            restActionSupplier.get().queue(success, failure);
        }
    }

    @Override
    public void onMessageDelete(GuildMessageDeleteEvent event) {

    }

    @Override
    public void cancel() {

    }


    @Override
    public RestActionExtension<Void> after(long delay, TimeUnit unit) {
        Checks.notNull(unit, "TimeUnit");
        Checks.positive(delay, "delay");
        this.delay = delay;
        this.unit = unit;
        return this;
    }

    @Override
    public RestActionExtension<Void> onSuccess(Consumer<Void> success) {
        this.success = success;
        return this;
    }

    @Override
    public RestActionExtension<Void> onFailure(Consumer<Throwable> failure) {
        this.failure = failure;
        return this;
    }

    @Override
    public RestActionExtension<Void> appendSuccess(Consumer<Void> success) {
        if (this.success == null) {
            return onSuccess(success);
        } else {
            return onSuccess(this.success.andThen(success));
        }
    }

    @Override
    public RestActionExtension<Void> appendFailure(Consumer<Throwable> failure) {
        if (this.failure == null) {
            return onFailure(RestAction.DEFAULT_FAILURE.andThen(failure));
        } else {
            return onFailure(this.failure.andThen(failure));
        }
    }
}
