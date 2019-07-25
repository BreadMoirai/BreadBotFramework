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

    CommandHandleBuilderInternal createCommand(Supplier<?> commandSupplier, Object result);

    List<CommandHandleBuilderInternal> createCommands(Class<?> commandClass);

    List<CommandHandleBuilderInternal> createCommands(Object commandObject);

    List<CommandHandleBuilderInternal> createCommands(Supplier<?> commandSupplier, Object result);

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
