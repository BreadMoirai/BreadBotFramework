package com.github.breadmoirai.bot.framework.command.buildernew;

import com.github.breadmoirai.bot.framework.BreadBotClient;
import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParser;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CommandHandleImpl implements CommandHandle {

    private final String[] keys;
    private final String name;
    private final String group;
    private final String description;
    private final BreadBotClient client;
    private final Function<Object, Object> commandSupplier;
    private final CommandParameter[] commandParameters;
    private final BiConsumer<Object, Object[]> commandFunction;
    private final Map<String, CommandHandle> subCommandMap;
    private final List<CommandPreprocessor> preprocessors;
    private final CommandPropertyMap propertyMap;

    public CommandHandleImpl(String[] keys,
                             String name,
                             String group,
                             String description,
                             BreadBotClient client,
                             Function<Object, Object> commandSupplier,
                             CommandParameter[] commandParameters,
                             BiConsumer<Object, Object[]> commandFunction,
                             Map<String, CommandHandle> subCommandMap,
                             List<CommandPreprocessor> preprocessors,
                             CommandPropertyMap propertyMap) {
        this.keys = keys;
        this.name = name;
        this.group = group;
        this.description = description;
        this.client = client;
        this.commandSupplier = commandSupplier;
        this.commandParameters = commandParameters;
        this.commandFunction = commandFunction;
        this.subCommandMap = subCommandMap.isEmpty() ? null : subCommandMap;
        this.preprocessors = preprocessors;
        this.propertyMap = propertyMap;
    }

    @Override
    public boolean handle(Object parent, CommandEvent event, Iterator<String> keyItr) {
        Object commandObj = commandSupplier.apply(parent);
        if (keyItr != null && keyItr.hasNext() && subCommandMap != null) {
            String next = keyItr.next().toLowerCase();
            if (subCommandMap.containsKey(next)) {
                CommandHandle subHandle = subCommandMap.get(next);
                if (event.isHelpEvent()) {
                    return subHandle.handle(commandObj, event, keyItr) || (subCommandMap.containsKey("help") && subCommandMap.get("help").handle(commandObj, event, null));
                } else {
                    return subHandle.handle(commandObj, event, keyItr);
                }
            }
        }
        final CommandParser parser = new CommandParser(event, this, event.getArguments(), commandParameters);
        if (parser.mapAll()) {
            commandFunction.accept(parent, parser.getResults());
            return true;
        }
        return false;
    }

    @Override
    public BreadBotClient getClient() {
        return client;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public CommandPropertyMap getPropertyMap() {
        return propertyMap;
    }

    @Override
    public Map<String, CommandHandle> getSubCommandMap() {
        return subCommandMap;
    }
}
