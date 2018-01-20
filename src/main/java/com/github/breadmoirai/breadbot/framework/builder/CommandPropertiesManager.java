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

public interface CommandPropertiesManager {

    /**
     * Removes any existing behavior attached to this propertyType.
     *
     * @param propertyType the class of the property.
     * @return this.
     */
    CommandPropertiesManager clearCommandModifiers(Class<?> propertyType);

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier, the passed BiConsumer is appended to the end of it so that it is applied to the builder after the existing one has been applied.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    <T> CommandPropertiesManager addCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator);

    /**
     * Removes any existing behavior attached to this parameter.
     *
     * @param parameterType the class of the parameter.
     * @return this.
     */
    CommandPropertiesManager clearParameterModifiers(Class<?> parameterType);

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier, the passed BiConsumer is appended to the end of it so that it is applied to the builder after the existing one has been applied.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    <T> CommandPropertiesManager addParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator);

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
     * @param <T>          the property type.
     * @return a BiConsumer if present, otherwise {@code null}.
     * @see #clearCommandModifiers(Class)
     * @see #addCommandModifier(Class, BiConsumer)
     * @see #applyCommandModifier(Class, CommandHandleBuilder)
     */
    <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType);

    /**
     * Applies a modifier that is associated with a certain {@code propertyType} to the passed {@code builder}.
     * If a modifier is not found the {@code builder} is not modified.
     *
     *
     * @param propertyType the property class
     * @param builder      the CommandHandleBuilder to be modified
     * @param <T>          the property type
     * @see #clearCommandModifiers(Class) (Class)
     * @see #addCommandModifier(Class, BiConsumer)
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
     * @param <T>          the property type.
     * @return a BiConsumer if present, otherwise {@code null}.
     * @see #clearParameterModifiers(Class)
     * @see #addParameterModifier(Class, BiConsumer)
     * @see #applyParameterModifier(Class, CommandParameterBuilder)
     */
    <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType);

    /**
     * Applies a modifier that is associated with a certain {@code propertyType} to the passed {@code builder}.
     * If a modifier is not found the {@code builder} is not modified.
     *
     * @param propertyType the property class
     * @param builder      the CommandHandleBuilder to be modified
     * @param <T>          the property type
     * @see #clearParameterModifiers(Class) (Class, BiConsumer)
     * @see #addParameterModifier(Class, BiConsumer)
     * @see #getParameterModifier(Class)
     */
    <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder);

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the property.
     *
     * @param identifier   a name for the preprocessor
     * @param propertyType the property class
     * @param function     a {@link CommandPreprocessorFunction#process CommandPreprocessorFunction}
     */
    default CommandPropertiesManager associatePreprocessor(String identifier, Class<?> propertyType, CommandPreprocessorFunction function) {
        addCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(new CommandPreprocessor(identifier, function)));
        return this;
    }

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the property.
     *
     * @param identifier   a name for the preprocessor
     * @param propertyType the property class
     * @param factory      a function that generates a preprocessor based upon the value of the property
     * @param <T>          the property type
     */
    default <T> CommandPropertiesManager associatePreprocessorFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorFunction> factory) {
        addCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(new CommandPreprocessor(identifier, factory.apply(t))));
        return this;
    }

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the property.
     *
     * @param identifier   a name for the preprocessor
     * @param propertyType the property class
     * @param predicate    a {@link java.util.function.Predicate Predicate}{@literal <}{@link CommandEvent CommandEvent}{@literal >} that returns {@code true} when the command should continue to execute, {@code false} otherwise
     */
    default CommandPropertiesManager associatePreprocessorPredicate(String identifier, Class<?> propertyType, CommandPreprocessorPredicate predicate) {
        addCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(new CommandPreprocessor(identifier, predicate)));
        return this;
    }

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the property.
     *
     * @param identifier   a name for the preprocessor
     * @param propertyType the property class
     * @param factory      a function that generates a preprocessor predicate based upon the value of the property
     * @param <T>          the property type
     */
    default <T> CommandPropertiesManager associatePreprocessorPredicateFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorPredicate> factory) {
        addCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(new CommandPreprocessor(identifier, factory.apply(t))));
        return this;
    }

    List<String> getPreprocessorPriorityList();

    CommandPropertiesManager setPreprocessorPriority(String... identifiers);

    CommandPropertiesManager setPreprocessorPriority(List<String> identifierList);

    Comparator<CommandPreprocessor> getPriorityComparator();
}
