package com.github.breadmoirai.bot.framework.command.arg.impl;

import com.github.breadmoirai.bot.framework.command.arg.ArgumentMapper;
import com.github.breadmoirai.bot.framework.command.arg.CommandArgument;
import com.github.breadmoirai.bot.framework.command.arg.CommandArgumentList;
import com.github.breadmoirai.bot.framework.command.arg.CommandParameter;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import gnu.trove.set.TIntSet;

import java.util.function.BiConsumer;

public class ArgumentParameterImpl implements CommandParameter {

    private Class<?> type;
    private Class<?> targetType;
    private int flags;
    private int[] indexes;
    private ArgumentMapper<?> mapper;
    private boolean mustBePresent;
    private BiConsumer<CommandEvent, CommandParameter> onParamNotFound;

    public ArgumentParameterImpl(Class<?> type, Class<?> targetType, int flags, int[] indexes, ArgumentMapper<?> mapper, boolean mustBePresent, BiConsumer<CommandEvent, CommandParameter> onParamNotFound) {
        this.type = type;
        this.flags = flags;
        this.indexes = indexes;
        this.mapper = mapper;
        this.mustBePresent = mustBePresent;
        this.onParamNotFound = onParamNotFound;
    }

    @Override
    public Object map(CommandArgumentList list, TIntSet set) {
        for (int i = 0; i < list.size(); i++) {
            if (set.contains(i)) continue;
            CommandArgument arg = list.get(i);
            if (arg.isOfType(type))
        }
    }

    public Class<?> getType() {
        return type;
    }
}
