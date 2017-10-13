package com.github.breadmoirai.breadbot.waiter;

import java.util.function.Predicate;

public class PredicateTracker<T> implements Predicate<T> {
    private final Predicate<T> predicate;
    private boolean passed;

    public PredicateTracker(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(T t) {
        if (!passed) {
            passed = predicate.test(t);
            return passed;
        } else {
            return predicate.test(t);
        }
    }

    public boolean isPassed() {
        return passed;
    }

    @Override
    public Predicate<T> and(Predicate<? super T> other) {
        return predicate.and(other);
    }

    @Override
    public Predicate<T> negate() {
        return predicate.negate();
    }

    @Override
    public Predicate<T> or(Predicate<? super T> other) {
        return predicate.or(other);
    }

    public static <T1> Predicate<T1> isEqual(Object targetRef) {
        return Predicate.isEqual(targetRef);
    }
}
