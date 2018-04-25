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

package com.github.breadmoirai.breadbot.framework.command.internal;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPropertyMap;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;
import com.github.breadmoirai.breadbot.framework.inject.BreadInjector;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class CommandHandleImpl implements CommandHandle {

    private final String[] keys;
    private final String name;
    private final String group;
    private final String description;
    private final Object declaringObject;
    private final Class<?> declaringClass;
    private final Method declaringMethod;
    //    private final BreadBotClientImpl client;
    private final CommandObjectFactory commandSupplier;
    private final CommandParameter[] commandParameters;
    private final InvokableCommand invokableCommand;
    private final CommandResultHandler<?> resultHandler;
    private final Map<String, CommandHandleImpl> subCommandMap;
    private final List<CommandPreprocessor> preprocessors;
    private final CommandPropertyMap propertyMap;
    private final Pattern splitRegex;
    private final int splitLimit;
    private final boolean isHelp;
    private Command superCommand;

    private BreadInjector.Injector injector;

    public CommandHandleImpl(String[] keys,
                             String name,
                             String group,
                             String description,
                             Object declaringObject,
                             Class<?> declaringClass,
                             Method declaringMethod,
//                             BreadBotClientImpl client,
                             CommandObjectFactory commandSupplier,
                             CommandParameter[] commandParameters,
                             InvokableCommand commandFunction,
                             CommandResultHandler<?> resultHandler,
                             Map<String, CommandHandleImpl> subCommandMap,
                             List<CommandPreprocessor> preprocessors,
                             CommandPropertyMap propertyMap,
                             Pattern splitRegex,
                             int splitLimit,
                             Command superCommand,
                             BreadInjector.Injector injector) {
        this.keys = keys;
        this.name = name;
        this.group = group;
        this.description = description;
        this.declaringObject = declaringObject;
        this.declaringClass = declaringClass;
        this.declaringMethod = declaringMethod;
//        this.client = client;
        this.commandSupplier = commandSupplier;
        this.commandParameters = commandParameters;
        this.invokableCommand = commandFunction;
        this.resultHandler = resultHandler;
        this.subCommandMap = subCommandMap;
        this.preprocessors = preprocessors;
        this.propertyMap = propertyMap;
        this.splitRegex = splitRegex;
        this.splitLimit = splitLimit;
        this.superCommand = superCommand;
        this.isHelp = Arrays.stream(keys).anyMatch(s -> s.equalsIgnoreCase("help"));
        this.injector = injector;
    }

    @Override
    public boolean handle(CommandEventInternal event, Iterator<String> keyItr) {
        if (isHelp && event.isHelpEvent()) {
            return runThis(event);
        }
        if (keyItr != null && keyItr.hasNext() && subCommandMap != null) {
            String next = keyItr.next().toLowerCase();
            if (subCommandMap.containsKey(next)) {
                Command subHandle = subCommandMap.get(next);
                if (event.isHelpEvent()) {
                    return subHandle.handle(event, keyItr) || (subCommandMap.containsKey("help") && subCommandMap.get("help").handle(event, null));
                } else {
                    return subHandle.handle(event, keyItr) || runThis(event);
                }
            }
        }
        if (event.isHelpEvent() && subCommandMap != null) {
            return subCommandMap.containsKey("help") && subCommandMap.get("help").handle(event, null);
        } else {
            return runThis(event);
        }
    }

    private boolean runThis(CommandEventInternal event) {
        event.setCommand(this);
        Object commandObj = commandSupplier.get();
        if (commandObj == null) return false;
        if (injector != null) {
            try {
                injector.inject(commandObj);
            } catch (IllegalAccessException e) {
                // this should not be thrown unless someone else uses reflection and sets accessible to false
                throw new RuntimeException("Access to field was revoked", e);
            }
        }
        if (invokableCommand != null) {
            final CommandParser parser = new CommandParser(event, this, splitRegex == null ? event.getArguments() : event.createNewArgumentList(splitRegex, splitLimit), getParameters());
            final CommandRunner runner = new CommandRunner(commandObj, event, invokableCommand, parser, this, resultHandler);
            final CommandProcessStack commandProcessStack = new CommandProcessStack(commandObj, this, event, preprocessors, runner);
            commandProcessStack.runNext();
            return commandProcessStack.result();
        } else return false;
    }

    @Override
    public String[] getKeys() {
        if (keys == null) return null;
        return Arrays.copyOf(keys, keys.length);
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
    public Object getDeclaringObject() {
        return declaringObject;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public Method getDeclaringMethod() {
        return declaringMethod;
    }

    @Override
    public CommandPropertyMap getPropertyMap() {
        return propertyMap;
    }

    @Override
    public List<CommandPreprocessor> getPreprocessors() {
        return Collections.unmodifiableList(preprocessors);
    }

    @Override
    public Map<String, Command> getChildren() {
        return Collections.unmodifiableMap(subCommandMap);
    }

    @Override
    public Command getParent() {
        return superCommand;
    }

    @Override
    public CommandParameter[] getParameters() {
        return Arrays.copyOf(commandParameters, commandParameters.length);
    }

    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int indent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("CommandHandle{\n");
        tab(sb, indent + 2);
        sb.append("keys: ").append(Arrays.toString(getKeys())).append('\n');
        tab(sb, indent + 2);
        sb.append("name: ").append(getName()).append('\n');
        tab(sb, indent + 2);
        sb.append("group: ").append(getGroup()).append('\n');
        tab(sb, indent + 2);
        sb.append("desc: ").append(getDescription()).append('\n');
        tab(sb, indent + 2);
        sb.append("source: ");
        if (getDeclaringMethod() != null) {
            sb.append(getDeclaringClass().getName()).append('#').append(getDeclaringMethod().getName());
        } else {
            sb.append(getDeclaringClass());
        }
        sb.append('\n');
        if (subCommandMap != null) {
            tab(sb, indent + 2);
            sb.append("subcommands: [\n");
            StringJoiner sj = new StringJoiner(",\n");
            for (CommandHandleImpl commandHandle : subCommandMap.values()) {
                sj.add(commandHandle.toString(indent + 4));
            }
            tab(sb, indent + 4);
            sb.append(sj);
            tab(sb, indent + 2);
            sb.append("]\n");
        }
        tab(sb, indent);
        sb.append("}");

        return sb.toString();
    }

    private void tab(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
    }
}
