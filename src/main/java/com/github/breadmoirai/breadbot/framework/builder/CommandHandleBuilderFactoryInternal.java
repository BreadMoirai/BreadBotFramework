package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface CommandHandleBuilderFactoryInternal extends CommandHandleBuilderFactory {

    @Override
    default CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        return createCommandHandle(onCommand);
    }

    @Override
    default CommandHandleBuilder createCommand(Supplier<Object> commandSupplier) {
        return createCommandHandle(commandSupplier);
    }

    @Override
    default CommandHandleBuilder createCommand(Class<?> commandClass) {
        return createCommandHandle(commandClass);
    }

    @Override
    default CommandHandleBuilder createCommand(Object commandObject) {
        return createCommandHandle(commandObject);
    }

    @Override
    default List<CommandHandleBuilder> createCommands(String packageName) {
        return Collections.unmodifiableList(createCommandHandles(packageName));
    }

    @Override
    default List<CommandHandleBuilder> createCommands(Class<?> commandClass) {
        return Collections.unmodifiableList(createCommandHandles(commandClass));
    }

    CommandHandleBuilderInternal createCommandHandle(Consumer<CommandEvent> onCommand);

    CommandHandleBuilderInternal createCommandHandle(Supplier<Object> commandSupplier);

    CommandHandleBuilderInternal createCommandHandle(Class<?> commandClass);

    CommandHandleBuilderInternal createCommandHandle(Object commandObject);

    List<CommandHandleBuilderInternal> createCommandHandles(Class<?> commandClass);

    List<CommandHandleBuilderInternal> createCommandHandles(String packageName);
}
