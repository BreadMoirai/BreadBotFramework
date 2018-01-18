/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.command.internal.builder;

import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandHandleImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandObjectFactory;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface CommandHandleBuilderInternal extends CommandHandleBuilder {

    CommandPropertyMapImpl getPropertyMap();

    CommandHandleImpl build(Command parent);

    void putCommandHandle(CommandHandleBuilderInternal handle);

    void putCommandHandles(Collection<CommandHandleBuilderInternal> commands);

    CommandHandleBuilderFactoryInternal getCommandFactory();

    CommandObjectFactory getObjectFactory();

    @Override
    default CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        CommandHandleBuilderInternal command = getCommandFactory().createCommand(onCommand);
        putCommandHandle(command);
        return command;
    }

    @Override
    default CommandHandleBuilder createCommand(Class commandClass) {
        CommandHandleBuilderInternal command = getCommandFactory().createCommand(commandClass);
        putCommandHandle(command);
        return command;
    }

    @Override
    default CommandHandleBuilder createCommand(Object commandObject) {
        CommandHandleBuilderInternal command = getCommandFactory().createCommand(commandObject);
        putCommandHandle(command);
        return command;
    }

    @Override
    default CommandHandleBuilder createCommand(Supplier commandSupplier) {
        CommandHandleBuilderInternal command = getCommandFactory().createCommand(commandSupplier);
        putCommandHandle(command);
        return command;
    }

    @Override
    default List<CommandHandleBuilder> createCommands(Class commandClass) {
        List<CommandHandleBuilderInternal> commands = getCommandFactory().createCommands(commandClass);
        putCommandHandles(commands);
        return Collections.unmodifiableList(commands);
    }

    @Override
    default List<CommandHandleBuilder> createCommands(Object commandObject) {
        List<CommandHandleBuilderInternal> commands = getCommandFactory().createCommands(commandObject);
        putCommandHandles(commands);
        return Collections.unmodifiableList(commands);
    }

    @Override
    default List<CommandHandleBuilder> createCommands(Supplier commandSupplier) {
        List<CommandHandleBuilderInternal> commands = getCommandFactory().createCommands(commandSupplier);
        putCommandHandles(commands);
        return Collections.unmodifiableList(commands);
    }

    @Override
    default List<CommandHandleBuilder> createCommands(String packageName) {
        List<CommandHandleBuilderInternal> commands = getCommandFactory().createCommands(packageName);
        putCommandHandles(commands);
        return Collections.unmodifiableList(commands);
    }

    @Override
    default List<CommandHandleBuilder> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        List<CommandHandleBuilderInternal> commands = getCommandFactory().createCommandsFromClasses(commandClasses);
        putCommandHandles(commands);
        return Collections.unmodifiableList(commands);
    }

    @Override
    default List<CommandHandleBuilder> createCommandsFromObjects(Collection<?> commandObjects) {
        List<CommandHandleBuilderInternal> commands = getCommandFactory().createCommandsFromObjects(commandObjects);
        putCommandHandles(commands);
        return Collections.unmodifiableList(commands);
    }

    @Override
    default List<CommandHandleBuilder> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier) {
        List<CommandHandleBuilderInternal> commands = getCommandFactory().createCommandsFromSuppliers(commandSupplier);
        putCommandHandles(commands);
        return Collections.unmodifiableList(commands);
    }
}