package com.github.breadmoirai.breadbot.framework.parameter.internal.collections;

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
