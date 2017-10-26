package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface CommandHandleBuilderFactoryInternal {

    CommandHandleBuilderInternal createCommand(Consumer<CommandEvent> onCommand);

    CommandHandleBuilderInternal createCommand(Class<?> commandClass);

    CommandHandleBuilderInternal createCommand(Object commandObject);

    CommandHandleBuilderInternal createCommand(Supplier<?> commandSupplier);

    List<CommandHandleBuilderInternal> createCommands(Class<?> commandClass);

    List<CommandHandleBuilderInternal> createCommands(Object commandObject);

    List<CommandHandleBuilderInternal> createCommands(Supplier<?> commandSupplier);

    List<CommandHandleBuilderInternal> createCommands(String packageName);

    default List<CommandHandleBuilderInternal> createCommandsFromClasses(Class<?>... commandClasses) {
        return createCommandsFromClasses(Arrays.asList(commandClasses));
    }

    default List<CommandHandleBuilderInternal> createCommandsFromObjects(Object... commandObjects) {
        return createCommandsFromObjects(Arrays.asList(commandObjects));
    }

    default List<CommandHandleBuilderInternal> createCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        return createCommandsFromSuppliers(Arrays.asList(commandSuppliers));
    }

    List<CommandHandleBuilderInternal> createCommandsFromClasses(Collection<Class<?>> commandClasses);

    List<CommandHandleBuilderInternal> createCommandsFromObjects(Collection<?> commandObjects);

    List<CommandHandleBuilderInternal> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier);

}
