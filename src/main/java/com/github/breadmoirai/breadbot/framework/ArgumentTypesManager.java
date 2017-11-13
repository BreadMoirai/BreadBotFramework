package com.github.breadmoirai.breadbot.framework;

import com.github.breadmoirai.breadbot.framework.internal.parameter.ArgumentParser;

public interface ArgumentTypesManager {

    /**
     * Returns the predicate mapper pair registered if found.
     *
     * @param type the class of the type as it was registered or one of the default types.
     * @param <T>  the type
     * @return an ArgumentParser if found. Else {@code null}.
     */
    <T> ArgumentParser<T> getParser(Class<T> type);
}
