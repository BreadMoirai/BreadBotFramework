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

import com.github.breadmoirai.breadbot.framework.response.RestActionExtension;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.Checks;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class RestActionExtensionImpl<T> implements RestActionExtension<T> {

    private long delay;
    private TimeUnit unit;
    private Consumer<T> success;
    private Consumer<Throwable> failure;

    @Override
    public RestActionExtension<T> after(long delay, TimeUnit unit) {
        Checks.notNull(unit, "TimeUnit");
        Checks.positive(delay, "delay");
        this.delay = delay;
        this.unit = unit;
        return this;
    }

    @Override
    public RestActionExtension<T> onSuccess(Consumer<T> success) {
        this.success = success;
        return this;
    }

    @Override
    public RestActionExtension<T> onFailure(Consumer<Throwable> failure) {
        this.failure = failure;
        return this;
    }

    @Override
    public RestActionExtension<T> appendSuccess(Consumer<T> success) {
        if (this.success == null) {
            return onSuccess(success);
        } else {
            return onSuccess(this.success.andThen(success));
        }
    }

    @Override
    public RestActionExtension<T> appendFailure(Consumer<Throwable> failure) {
        if (this.failure == null) {
            return onFailure(RestAction.DEFAULT_FAILURE.andThen(failure));
        } else {
            return onFailure(this.failure.andThen(failure));
        }
    }

    protected long getDelay() {
        return delay;
    }

    protected TimeUnit getUnit() {
        return unit;
    }

    protected Consumer<T> getSuccess() {
        return success;
    }

    protected Consumer<Throwable> getFailure() {
        return failure;
    }
}