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
package com.github.breadmoirai.breadbot.framework.response;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface RestActionExtension<T> {
    /**
     * Delays this action by the specified amount of time.
     *
     * @param delay the amount of time
     * @param unit  the unit of time
     * @return this
     */
    RestActionExtension<T> after(long delay, TimeUnit unit);

    /**
     * Sets a Consumer that will be called on the success of this action.
     *
     * @param success a consumer
     * @return this
     */
    RestActionExtension<T> onSuccess(Consumer<T> success);

    /**
     * Sets a Consumer that will be called on the failure of this action.
     *
     * <p> By default this will simply log and ignore the exception
     *
     * @param failure a consumer that accepts a throwable
     * @return this
     */
    RestActionExtension<T> onFailure(Consumer<Throwable> failure);

    /**
     * Appends a Consumer to any existing behavior.
     * If there is no success callback already set, this method behaves just as {@link #onSuccess(Consumer)}.
     *
     * @param success a consumer that is called after any existing consumers
     * @return this
     */
    RestActionExtension<T> appendSuccess(Consumer<T> success);

    /**
     * Appends a Consumer to any existing failure behavior.
     * If there is no failure callback already set, this consumer is called
     * after the default failure callback which is logging the exception.
     *
     * @param failure a consumer that accepts a Throwable
     * @return this
     */
    RestActionExtension<T> appendFailure(Consumer<Throwable> failure);

    /**
     * This method finalizes content fields and queues the action to Discord.
     */
    void send();

    /**
     * This function is the same as {@link #send()}
     */
    default void build() {
        send();
    }
}
