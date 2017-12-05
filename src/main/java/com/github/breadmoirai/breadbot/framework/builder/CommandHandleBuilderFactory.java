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

package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.CommandEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Todo write docs
 */
public interface CommandHandleBuilderFactory<T extends CommandHandleBuilderFactory> {

    /**
     * a self reference.
     *
     * @return this
     */
    T self();

    default T addCommand(Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        configurator.accept(createCommand(onCommand));
        return self();
    }

    default T addCommand(Class<?> commandClass, Consumer<CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        createCommandsFromClasses(commandClass).forEach(configurator);
        return self();
    }

    default T addCommand(Class<?> commandClass) {
        createCommandsFromClasses(commandClass);
        return self();
    }

    default T addCommand(Object commandObject, Consumer<CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        createCommandsFromObjects(commandObject).forEach(configurator);
        return self();
    }

    default T addCommand(Object commandObject) {
        createCommandsFromObjects(commandObject);
        return self();
    }

    default T addCommand(Supplier<?> commandSupplier, Consumer<CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        final List<CommandHandleBuilder> commands = createCommandsFromSuppliers(commandSupplier);
        commands.forEach(configurator);
        return self();
    }

    default T addCommand(Supplier<?> commandSupplier) {
        createCommandsFromSuppliers(commandSupplier);
        return self();
    }

    default T addCommands(String packageName, Consumer<CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        createCommands(packageName).forEach(configurator);
        return self();
    }

    default T addCommands(String packageName) {
        createCommands(packageName);
        return self();
    }

    default T addCommandsFromClasses(Consumer<CommandHandleBuilder> configurator, Class<?>... commandClasses) {
        Checks.notNull(configurator, "configurator");
        createCommandsFromClasses(commandClasses).forEach(configurator);
        return self();
    }

    default T addCommandsFromClasses(Class<?>... commandClasses) {
        createCommandsFromClasses(commandClasses);
        return self();
    }

    default T addCommandsFromObjects(Consumer<CommandHandleBuilder> configurator, Object... commandObjects) {
        Checks.notNull(configurator, "configurator");
        createCommandsFromObjects(commandObjects).forEach(configurator);
        return self();
    }

    default T addCommandsFromObjects(Object... commandObjects) {
        createCommandsFromObjects(commandObjects);
        return self();
    }

    default T addCommandsFromSuppliers(Consumer<CommandHandleBuilder> configurator, Supplier<?>... commandSuppliers) {
        Checks.notNull(configurator, "configurator");
        createCommandsFromSuppliers(commandSuppliers).forEach(configurator);
        return self();
    }

    default T addCommandsFromSuppliers(Supplier<?>... commandSuppliers) {
        createCommandsFromSuppliers(commandSuppliers);
        return self();
    }

    default T addCommandsFromClasses(Collection<Class<?>> commandClasses, Consumer<CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        createCommandsFromClasses(commandClasses).forEach(configurator);
        return self();
    }

    default T addCommandsFromClasses(Collection<Class<?>> commandClasses) {
        createCommandsFromClasses(commandClasses);
        return self();
    }

    default T addCommandsFromObjects(Collection<?> commandObjects, Consumer<CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        createCommandsFromObjects(commandObjects).forEach(configurator);
        return self();
    }

    default T addCommandsFromObjects(Collection<?> commandObjects) {
        createCommandsFromObjects(commandObjects);
        return self();
    }

    default T addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers, Consumer<CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        createCommandsFromSuppliers(commandSuppliers).forEach(configurator);
        return self();
    }

    default T addCommandsFromSuppliers(Collection<Supplier<?>> commandSuppliers) {
        createCommandsFromSuppliers(commandSuppliers);
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