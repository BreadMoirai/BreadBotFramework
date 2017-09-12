package com.github.breadmoirai.bot.framework.command.builder;

import com.github.breadmoirai.bot.framework.command.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.command.impl.CommandHandle;
import com.github.breadmoirai.bot.framework.error.NoSuchCommandException;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommandBuilder implements CommandHandleBuilder {

    private String name;
    private String[] keys;
    private List<CommandHandleBuilder> handleBuilders;
    private CommandPropertyMap properties;
    private boolean isPersistent;

    public CommandBuilder(Class<?> commandClass) {
        this.name = commandClass.getName();
        handleBuilders = new ArrayList<>();
        Arrays.stream(commandClass.getMethods())
                .filter(method -> method.getParameterCount() > 0)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .map(CommandMethodHandleBuilder::new)
                .forEach(handleBuilders::add);

    }

    public CommandBuilder(Object commandObj) {

    }

    /**
     * Sets the keys of this command. When the keys is set to {@code null}, if the provided class/object has multiple methods/classes, each one will be registered with their own keys.
     *
     * @param keys a var-arg of String. no spaces plz.
     * @return this obj
     */
    public CommandBuilder setKeys(String... keys) {
        this.keys = keys;
        return this;
    }

    /**
     * This determines whether each execution of this command will use the same obj or instantiate a new obj for each instance.
     * By default this value is set to {@code false}
     *
     * @param isPersistent a boolean
     * @return this obj
     */
    public CommandBuilder setPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
        return this;
    }

    /**
     * This configures the method with the specified name using the {@link java.util.function.Consumer} provided.
     *
     * @param methodName the name of the method. case-sensitive
     * @param consumer   a consumer that modifies the {@link com.github.breadmoirai.bot.framework.command.builder.CommandMethodHandleBuilder}
     * @return this obj
     */
    public CommandBuilder configureCommandMethod(String methodName, Consumer<CommandMethodHandleBuilder> consumer) {
        handleBuilders.stream()
                .filter(handleBuilder -> handleBuilder.getName().equals(methodName))
                .filter(obj -> obj instanceof CommandMethodHandleBuilder)
                .map(obj -> ((CommandMethodHandleBuilder) obj))
                .findFirst()
                .orElseThrow(() -> new NoSuchCommandException(methodName))
                .configure(consumer);
        return this;
    }

    /**
     * This configures an inner class with the specified name using the {@link java.util.function.Consumer} provided.
     *
     * @param className the name of the method. case-sensitive
     * @param consumer  a consumer that modifies the {@link com.github.breadmoirai.bot.framework.command.builder.CommandClassHandleBuilder}
     * @return this obj
     */
    public CommandBuilder configureCommandClass(String className, Consumer<CommandClassHandleBuilder> consumer) {
        handleBuilders.stream()
                .filter(handleBuilder -> handleBuilder.getName().equals(className))
                .filter(obj -> obj instanceof CommandClassHandleBuilder)
                .map(obj -> ((CommandClassHandleBuilder) obj))
                .findFirst()
                .orElseThrow(() -> new NoSuchCommandException(className))
                .configure(consumer);
        return this;
    }

    public CommandBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String[] getKeys() {
        return this.keys == null ? handleBuilders.stream().map(CommandHandleBuilder::getKeys).flatMap(Arrays::stream).toArray(String[]::new) : keys;
    }

    @Override
    public CommandHandle build() {
        return null;
    }
}