package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface CommandHandleBuilderFactory {
    void addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator);

    void addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator);

    void addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator);

    void addCommand(Supplier<Object> commandSupplier, Consumer<CommandHandleBuilder> configurator);

    void addCommands(String packageName, Consumer<CommandHandleBuilder> configurator);

    CommandHandleBuilderInternal createCommand(Consumer<CommandEvent> onCommand);

    CommandHandleBuilderInternal createCommand(Supplier<Object> commandSupplier);

    CommandHandleBuilderInternal createCommand(Class<?> commandClass);

    CommandHandleBuilderInternal createCommand(Object commandObject);

    List<CommandHandleBuilder> createCommands(String packageName);
}
