package com.github.breadmoirai.breadbot.util;

import java.util.function.Supplier;

@FunctionalInterface
public interface ExceptionalSupplier<T> {

    T get() throws Throwable;

    static <T> ExceptionalSupplier<T> convert(Supplier<T> supplier) {
        return supplier::get;
    }
}
