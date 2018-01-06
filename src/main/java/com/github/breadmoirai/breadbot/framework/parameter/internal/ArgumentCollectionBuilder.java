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

package com.github.breadmoirai.breadbot.framework.parameter.internal;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ArgumentCollectionBuilder {

    static <F, T> ArgumentCollectionBuilder of(Supplier<F> factory, BiConsumer<F, T> accumulator, Function<F, ?> finisher) {
        return new ArgumentCollectionBuilder() {

            private final F f = factory.get();

            @Override
            public void accept(Object o) {
                @SuppressWarnings("unchecked") final T t = (T) o;
                accumulator.accept(f, t);
            }

            @Override
            public Object build() {
                return finisher.apply(f);
            }
        };
    }

    void accept(Object o);

    Object build();

}
