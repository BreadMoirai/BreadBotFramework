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

package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface CommandPropertiesManager<R> extends SelfReference<R> {

    /**
     * Removes any existing behavior attached to this propertyType.
     *
     * @param propertyType the class of the property.
     * @return this.
     */
    R clearCommandModifiers(Class<?> propertyType);

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * This behavior is added onto any existing behavior.
     * When a command is registered with that property (usually an annotation), the provided {@code configurator} is
     * applied.
     * If there are already commands registered, the {@code configurator} is applied to those as well.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link BiConsumer BiConsumer}.
     * The first argument is the property.
     * The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T> the propertyType
     * @return this, for chaining.
     */
    <T> R bindCommandModifier(Class<T> propertyType,
                              BiConsumer<T, CommandHandleBuilder> configurator);

    /**
     * Removes any existing behavior attached to this parameter.
     *
     * @param parameterType the class of the parameter.
     * @return this, for chaining.
     */
    R clearParameterModifiers(Class<?> parameterType);

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * This behavior is added onto any existing behavior.
     * When a parameter of the provided {@code propertyType} is found, the provided {@code configurator} is applied.
     * If there are already parameters of that type registered, the {@code configurator} is applied to those as well.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link BiConsumer BiConsumer}.
     * The first argument is the property.
     * The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T> the propertyType
     * @return this, for chaining.
     */
    <T> R bindParameterModifier(Class<T> propertyType,
                                BiConsumer<T, CommandParameterBuilder> configurator);

    /**
     * Applies modifiers to a CommandHandleBuilder based on whether the handle contains a property.
     *
     * @param builder the builder to modify
     */
    void applyModifiers(CommandHandleBuilder builder);

    /**
     * Retrieves a BiConsumer that is used to modify a CommandHandleBuilder.
     *
     * @param propertyType the class of the Property.
     * @param <T> the property type.
     * @return a BiConsumer if present, otherwise {@code null}.
     * @see #clearCommandModifiers(Class)
     * @see #bindCommandModifier(Class, BiConsumer)
     * @see #applyCommandModifier(Class, CommandHandleBuilder)
     */
    <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType);

    /**
     * Applies a modifier that is associated with a certain {@code propertyType} to the passed {@code builder}.
     * If a modifier is not found the {@code builder} is not modified.
     *
     * @param propertyType the property class
     * @param builder the CommandHandleBuilder to be modified
     * @param <T> the property type
     * @see #clearCommandModifiers(Class) (Class)
     * @see #bindCommandModifier(Class, BiConsumer)
     * @see #getCommandModifier(Class)
     */
    <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder);

    /**
     * Applies modifiers to a CommandParameterBuilder based on whether the handle contains a property.
     *
     * @param builder the builder to modify
     */
    void applyModifiers(CommandParameterBuilder builder);

    /**
     * Retrieves a BiConsumer that is used to modify a CommandParameterBuilder.
     *
     * @param propertyType the class of the Property.
     * @param <T> the property type.
     * @return a BiConsumer if present, otherwise {@code null}.
     * @see #clearParameterModifiers(Class)
     * @see #bindParameterModifier(Class, BiConsumer)
     * @see #applyParameterModifier(Class, CommandParameterBuilder)
     */
    <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType);

    /**
     * Applies a modifier that is associated with a certain {@code propertyType} to the passed {@code builder}.
     * If a modifier is not found the {@code builder} is not modified.
     *
     * @param propertyType the property class
     * @param builder the CommandHandleBuilder to be modified
     * @param <T> the property type
     * @see #clearParameterModifiers(Class) (Class, BiConsumer)
     * @see #bindParameterModifier(Class, BiConsumer)
     * @see #getParameterModifier(Class)
     */
    <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder);

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the
     * property.
     *
     * @param identifier a name for the preprocessor
     * @param propertyType the property class
     * @param function a {@link CommandPreprocessorFunction#process CommandPreprocessorFunction}
     * @return this, for chaining.
     */
    default R bindPreprocessor(String identifier, Class<?> propertyType,
                               CommandPreprocessorFunction function) {
        bindCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(
                new CommandPreprocessor(identifier, function)));
        return self();
    }

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the
     * property.
     *
     * @param identifier a name for the preprocessor
     * @param propertyType the property class
     * @param factory a function that generates a preprocessor based upon the value of the property
     * @param <T> the property type
     * @return this, for chaining.
     */
    default <T> R bindPreprocessorFactory(String identifier, Class<T> propertyType,
                                          Function<T, CommandPreprocessorFunction> factory) {
        bindCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(
                new CommandPreprocessor(identifier, factory.apply(t))));
        return self();
    }

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the
     * property.
     *
     * @param identifier a name for the preprocessor
     * @param propertyType the property class
     * @param predicate a {@link java.util.function.Predicate Predicate}{@literal <}{@link CommandEvent
     * CommandEvent}{@literal >} that returns {@code true} when the command should continue to execute, {@code false}
     * otherwise
     * @return this, for chaining.
     */
    default R bindPreprocessorPredicate(String identifier, Class<?> propertyType,
                                        CommandPreprocessorPredicate predicate) {
        bindCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(
                new CommandPreprocessor(identifier, predicate)));
        return self();
    }

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the
     * property.
     *
     * @param identifier a name for the preprocessor
     * @param propertyType the property class
     * @param factory a function that generates a preprocessor predicate based upon the value of the property
     * @param <T> the property type
     * @return this.
     */
    default <T> R bindPreprocessorPredicateFactory(String identifier, Class<T> propertyType,
                                                   Function<T, CommandPreprocessorPredicate>
                                                                                  factory) {
        bindCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(
                new CommandPreprocessor(identifier, factory.apply(t))));
        return self();
    }

    List<String> getPreprocessorPriorityList();

    R setPreprocessorPriority(String... identifiers);

    /**
     * Sets the order in which preprocessors are evaluated
     *
     * @param identifierList a list of strings that contain the names of each preprocessor. If there is a null value in
     * the list, all unmatched preprocessors will be put in that spot.
     * @return this.
     */
    R setPreprocessorPriority(List<String> identifierList);

    Comparator<CommandPreprocessor> getPriorityComparator();
}
