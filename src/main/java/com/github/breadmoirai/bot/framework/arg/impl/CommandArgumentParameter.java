package com.github.breadmoirai.bot.framework.arg.impl;

import com.github.breadmoirai.bot.framework.arg.ArgumentParameter;
import com.github.breadmoirai.bot.framework.arg.CommandArgumentList;
import gnu.trove.set.TIntSet;

public class CommandArgumentParameter implements ArgumentParameter {

    private final Class<?> type;

    public CommandArgumentParameter(Class<?> type) {
        this.type = type;
    }


    @Override
    public Object map(CommandArgumentList list, CommandArgumentList.ArgumentIterator itr, TIntSet set) {
        int i = itr.nextIndex(type);
        while (!set.add(i)) {
            i = itr.nextIndex(type);
        }
        return i != -1 ? list.get(i) : null;
    }
}
