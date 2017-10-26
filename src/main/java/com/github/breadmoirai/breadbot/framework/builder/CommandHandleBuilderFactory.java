package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Todo write docs
 */
public interface CommandHandleBuilderFactory<T> {

    /**
     * the self. This is here so I can encapsulate out some methods.
     *
     * @return this
     */
    T self();

    default T addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(onCommand));
        return self();
    }

    default T addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandClass));
        return self();
    }

    default T addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandObject));
        return self();
    }

    default T addCommand(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        configurator.accept(createCommand(commandSupplier));
        return self();
    }

    default T addCommands(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        createCommands(commandClass).forEach(configurator);
        return self();
    }

    default T addCommands(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        createCommands(commandObject).forEach(configurator);
        return self();
    }

    default T addCommands(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        createCommands(commandSupplier).forEach(configurator);
        return self();
    }

    default T addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        createCommands(packageName).forEach(configurator);
        return self();
    }

    default T addCommandsFromClasses(Consumer<CommandHandleBuilder> configurator, Class<?>... commandClasses) {
        createCommandsFromClasses(commandClasses).forEach(configurator);
        return self();
    }

    default T addCommandsFromObjects(Consumer<CommandHandleBuilder> configurator, Object... commandObjects) {
        createCommandsFromObjects(commandObjects).forEach(configurator);
        return self();
    }

    default T addCommandsFromSuppliers(Consumer<CommandHandleBuilder> configurator, Supplier<?>... commandSuppliers) {
        createCommandsFromSuppliers(commandSuppliers).forEach(configurator);
        return self();
    }

    default T addCommandsFromClasses(Collection<Class<?>> commandClasses, Consumer<CommandHandleBuilder> configurator) {
        createCommandsFromClasses(commandClasses).forEach(configurator);
        return self();
    }

    default T addCommandsFromObjects(Collection<?> commandObjects, Consumer<CommandHandleBuilder> configurator) {
        createCommandsFromObjects(commandObjects).forEach(configurator);
        return self();
    }

    default T addCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        createCommandsFromSuppliers(commandSupplier).forEach(configurator);
        return self();
    }

    CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand);

    CommandHandleBuilder createCommand(Class<?> commandClass);

    CommandHandleBuilder createCommand(Object commandObject);

    CommandHandleBuilder createCommand(Supplier<?> commandSupplier);

    List<CommandHandleBuilder> createCommands(Class<?> commandClass);

    List<CommandHandleBuilder> createCommands(Object commandObject);

    List<CommandHandleBuilder> createCommands(Supplier<?> commandSupplier);

    List<CommandHandleBuilder> createCommands(String packageName);

    default List<CommandHandleBuilder> createCommandsFromClasses(Class<?>... commandClasses) {
        return createCommandsFromClasses(Arrays.asList(commandClasses));
    }

    default List<CommandHandleBuilder> createCommandsFromObjects(Object... commandObjects) {
        return createCommandsFromObjects(Arrays.asList(commandObjects));
    }

    default List<CommandHandleBuilder> createCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        return createCommandsFromSuppliers(Arrays.asList(commandSuppliers));
    }

    List<CommandHandleBuilder> createCommandsFromClasses(Collection<Class<?>> commandClasses);

    List<CommandHandleBuilder> createCommandsFromObjects(Collection<?> commandObjects);

    List<CommandHandleBuilder> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier);
}