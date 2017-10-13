package com.github.breadmoirai.breadbot.util;

import java.util.Objects;

/**
 * Represents a 3-arity specialization of {@link java.util.function.Consumer}.
 * <p>
 * <p>This is a {@link java.lang.FunctionalInterface}
 * whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @param <V> the type of the third argument to the operation
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);

        return (l, m ,r) -> {
            accept(l, m, r);
            after.accept(l, m, r);
        };
    }

}
