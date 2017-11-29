package com.github.breadmoirai.breadbot.framework.internal.command.builder;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandHandleImpl;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandObjectFactory;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandPropertyMapImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface CommandHandleBuilderInternal extends CommandHandleBuilder {

    CommandPropertyMapImpl getPropertyMap();

    CommandHandleImpl build();

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