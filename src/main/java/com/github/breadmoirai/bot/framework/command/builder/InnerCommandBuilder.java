/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.bot.framework.command.builder;

import com.github.breadmoirai.bot.framework.command.*;
import com.github.breadmoirai.bot.framework.command.impl.CommandHandle;
import com.github.breadmoirai.bot.framework.command.impl.InnerCommandImpl;
import com.github.breadmoirai.bot.framework.error.NoSuchCommandException;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import net.dv8tion.jda.core.utils.Checks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class InnerCommandBuilder extends CommandHandleBuilder {

    private String[] keys;
    private List<CommandHandleBuilder> handleBuilders;
    private final Class<?> innerClass;

    public InnerCommandBuilder(Class<?> innerClass) {
        super(innerClass.getSimpleName());
        this.innerClass = innerClass;
        getPropertyBuilder().putAnnotations(innerClass.getAnnotations());

        handleBuilders = new ArrayList<>();
        Arrays.stream(innerClass.getMethods())
                .filter(method -> method.getParameterCount() > 0)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.isAnnotationPresent(Command.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .map(CommandMethodBuilder::new)
                .forEach(handleBuilders::add);
        Arrays.stream(innerClass.getClasses())
                .filter(aClass -> aClass.isAnnotationPresent(Command.class))
                .filter(aClass -> !Modifier.isStatic(aClass.getModifiers()))
                .map(InnerCommandBuilder::new)
                .forEach(handleBuilders::add);
    }

    /**
     * Sets the keys of this command. When the keys is set to {@code null}, if the provided class/object has multiple methods/classes, each one will be registered with their own keys.
     *
     * @param keys a var-arg of String. no spaces plz.
     * @return this obj
     */
    public InnerCommandBuilder setKeys(String... keys) {
        for (String key : keys) {
            Checks.notEmpty(key, "Command Key for " + innerClass.getName());
        }
        this.keys = keys;
        return this;
    }

    /**
     * This configures the method with the specified name using the {@link java.util.function.Consumer} provided.
     *
     * @param methodName the name of the method. case-sensitive
     * @param consumer   a consumer that modifies the {@link CommandMethodBuilder}
     * @return this obj
     */
    public InnerCommandBuilder configureCommandMethod(String methodName, Consumer<CommandMethodBuilder> consumer) {
        handleBuilders.stream()
                .filter(handleBuilder -> handleBuilder.getName().equals(methodName))
                .filter(obj -> obj instanceof CommandMethodBuilder)
                .map(obj -> ((CommandMethodBuilder) obj))
                .findFirst()
                .orElseThrow(() -> new NoSuchCommandException(methodName))
                .configure(consumer);
        return this;
    }

    /**
     * This configures an inner class with the specified name using the {@link java.util.function.Consumer} provided.
     *
     * @param className the name of the method. case-sensitive
     * @param consumer  a consumer that modifies the {@link InnerCommandBuilder}
     * @return this obj
     */
    public InnerCommandBuilder configureCommandClass(String className, Consumer<InnerCommandBuilder> consumer) {
        handleBuilders.stream()
                .filter(handleBuilder -> handleBuilder.getName().equals(className))
                .filter(obj -> obj instanceof InnerCommandBuilder)
                .map(obj -> ((InnerCommandBuilder) obj))
                .findFirst()
                .orElseThrow(() -> new NoSuchCommandException(className))
                .configure(consumer);
        return this;
    }


    @Override
    public String[] getKeys() {
        return keys != null ? keys : new String[]{innerClass.getSimpleName().toLowerCase()};
    }

    @Override
    public InnerCommandBuilder putProperty(Object property) {
        super.putProperty(property);
        return this;
    }

    @Override
    public <T> InnerCommandBuilder putProperty(Class<? super T> type, T property) {
        super.putProperty(type, property);
        return this;
    }

    @Override
    public InnerCommandBuilder setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public CommandHandle build() {
        final HashMap<String, CommandHandle> handleMap = new HashMap<>();
        for (CommandHandleBuilder handleBuilder : handleBuilders) {
            final CommandHandle handle = handleBuilder.build();
            for (String key : handle.getKeys()) {
                handleMap.put(key, handle);
            }
        }
        final CommandPropertyMap propertyMap = getPropertyBuilder().build();
        final String[] keys = getKeys();
        final List<CommandPreprocessor> preprocessorList = getPreprocessorList();
        final Function<Object, Object> supplier;
        final MethodHandle constructor;
        try {
            constructor = MethodHandles.publicLookup().findConstructor(innerClass, MethodType.methodType(void.class, innerClass.getEnclosingClass()));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Inner Class " + innerClass.getName() + " does not have a public no-arg constructor.");
        }
        return new InnerCommandImpl(getName(), getKeys(), constructor, handleMap, propertyMap, preprocessorList);
    }

    public InnerCommandBuilder configure(Consumer<InnerCommandBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    @Override
    public InnerCommandBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        super.addPreprocessorFunction(identifier, function);
        return this;
    }

    @Override
    public InnerCommandBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        super.addPreprocessorPredicate(identifier, predicate);
        return this;
    }

    @Override
    public InnerCommandBuilder addPreprocessors(Iterable<CommandPreprocessor> preprocessors) {
        super.addPreprocessors(preprocessors);
        return this;
    }

    @Override
    public InnerCommandBuilder sortPreprocessors() {
        super.sortPreprocessors();
        return this;
    }

    @Override
    public InnerCommandBuilder addAssociatedPreprocessors() {
        super.addAssociatedPreprocessors();
        return this;
    }
}
