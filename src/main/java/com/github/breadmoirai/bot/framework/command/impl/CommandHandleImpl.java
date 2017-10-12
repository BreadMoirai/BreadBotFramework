package com.github.breadmoirai.bot.framework.command.impl;

import com.github.breadmoirai.bot.framework.BreadBotClient;
import com.github.breadmoirai.bot.framework.command.CommandHandle;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParameter;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParser;
import com.github.breadmoirai.bot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandHandleImpl implements CommandHandle {

    private final String[] keys;
    private final String name;
    private final String group;
    private final String description;
    private final BreadBotClient client;
    private final CommandObjectFactory commandSupplier;
    private final CommandParameter[] commandParameters;
    private final InvokableCommand invokableCommand;
    private final Map<String, CommandHandle> subCommandMap;
    private final List<CommandPreprocessor> preprocessors;
    private final CommandPropertyMap propertyMap;

    public CommandHandleImpl(String[] keys,
                             String name,
                             String group,
                             String description,
                             BreadBotClient client,
                             CommandObjectFactory commandSupplier,
                             CommandParameter[] commandParameters,
                             InvokableCommand commandFunction,
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
        this.invokableCommand = commandFunction;
        this.subCommandMap = subCommandMap.isEmpty() ? null : subCommandMap;
        this.preprocessors = preprocessors;
        this.propertyMap = propertyMap;
    }

    @Override
    public boolean handle(Object parent, CommandEvent event, Iterator<String> keyItr) {
        final Object commandObj;
        try {
            commandObj = commandSupplier.create(parent);
        } catch (Throwable throwable) {
            //todo log this
            throwable.printStackTrace();
            return false;
        }
        if (keyItr != null && keyItr.hasNext() && subCommandMap != null) {
            String next = keyItr.next().toLowerCase();
            if (subCommandMap.containsKey(next)) {
                CommandHandle subHandle = subCommandMap.get(next);
                if (event.isHelpEvent()) {
                    return subHandle.handle(commandObj, event, keyItr) || (subCommandMap.containsKey("help") && subCommandMap.get("help").handle(commandObj, event, null));
                } else {
                    return subHandle.handle(commandObj, event, keyItr) || runThis(commandObj, event);
                }
            }
        }
        return runThis(commandObj, event);
    }

    private boolean runThis(Object commandObj, CommandEvent event) {
        if (invokableCommand != null) {
            final CommandParser parser = new CommandParser(event, this, event.getArguments(), commandParameters);
            final CommandRunner runner = new CommandRunner(commandObj, event, invokableCommand, parser, Throwable::printStackTrace); //todo log this
            final CommandProcessStack commandProcessStack = new CommandProcessStack(commandObj, this, event, preprocessors, runner);
            commandProcessStack.runNext();
            return commandProcessStack.result();
        } else return false;
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
