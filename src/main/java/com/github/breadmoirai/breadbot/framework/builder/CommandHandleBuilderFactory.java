package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Todo write docs
 */
public interface CommandHandleBuilderFactory {
    default void addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(onCommand));
    }

    default void addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandClass));
    }

    default void addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandObject));
    }

    default void addCommand(Supplier<Object> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandSupplier));
    }

    default void addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        createCommands(packageName).forEach(configurator);
    }

    default void addCommands(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        createCommands(commandClass).forEach(configurator);
    }

    CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand);

    CommandHandleBuilder createCommand(Supplier<Object> commandSupplier);

    CommandHandleBuilder createCommand(Class<?> commandClass);

    CommandHandleBuilder createCommand(Object commandObject);

    List<CommandHandleBuilder> createCommands(Class<?> commandClass);

    List<CommandHandleBuilder> createCommands(String packageName);

}