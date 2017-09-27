package com.github.breadmoirai.bot.framework.command.builder;

import com.github.breadmoirai.bot.framework.command.*;
import com.github.breadmoirai.bot.framework.command.impl.CommandImpl;
import com.github.breadmoirai.bot.framework.error.CommandInitializationException;
import com.github.breadmoirai.bot.framework.error.NoSuchCommandException;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandBuilder extends CommandHandleBuilder {

    private List<CommandHandleBuilder> handleBuilders;
    private boolean isPersistent = false;
    private final Class<?> commandClass;
    private Object obj;

    public CommandBuilder(Class<?> commandClass) {
        super(commandClass.getSimpleName());
        this.commandClass = commandClass;
        getPropertyBuilder().setDefaultProperties(CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()));
        getPropertyBuilder().putAnnotations(commandClass.getAnnotations());

        handleBuilders = new ArrayList<>();
        Arrays.stream(commandClass.getMethods())
                .filter(method -> method.getParameterCount() > 0)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.isAnnotationPresent(Command.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .map(CommandMethodBuilder::new)
                .peek(commandMethodHandleBuilder -> commandMethodHandleBuilder.getPropertyBuilder().setDefaultProperties(getPropertyBuilder()))
                .forEach(handleBuilders::add);
        Arrays.stream(commandClass.getClasses())
                .filter(aClass -> aClass.isAnnotationPresent(Command.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .map(InnerCommandBuilder::new)
                .peek(cchb -> cchb.getPropertyBuilder().setDefaultProperties(getPropertyBuilder()))
                .forEach(handleBuilders::add);
        if (handleBuilders.isEmpty()) {
            Arrays.stream(commandClass.getMethods())
                    .filter(method -> method.getParameterCount() > 0)
                    .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                    .filter(method -> !Modifier.isStatic(method.getModifiers()))
                    .map(CommandMethodBuilder::new)
                    .peek(cmhb -> cmhb.getPropertyBuilder().setDefaultProperties(getPropertyBuilder()))
                    .peek(cmhb -> {
                        if (getPropertyBuilder().containsProperty(Command.class)) {
                            final Command property = getPropertyBuilder().getProperty(Command.class);
                            if (property.value().length != 0) {
                                cmhb.setKeys(property.value());
                                return;
                            }
                        }
                        final String simpleName = commandClass.getSimpleName().toLowerCase();
                        if (simpleName.endsWith("command")) {
                            cmhb.setKeys(simpleName.substring(0, simpleName.length() - 7));
                        } else {
                            cmhb.setKeys(simpleName);
                        }
                    })
                    .forEach(handleBuilders::add);
        }
    }

    public CommandBuilder(Object commandObj) {
        this(commandObj.getClass());
        obj = commandObj;
    }

    public Class<?> getCommandClass() {
        return commandClass;
    }

    /**
     * Sets the keys of this command. When the keys is set to {@code null}, if the provided class/object has multiple methods/classes, each one will be registered with their own keys.
     *
     * @param keys a var-arg of String. no spaces plz.
     * @return this obj
     */
    @Override
    public CommandBuilder setKeys(String... keys) {
        super.setKeys(keys);
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
     * @param consumer   a consumer that modifies the {@link CommandMethodBuilder}
     * @return this obj
     */
    public CommandBuilder configureCommandMethod(String methodName, Consumer<CommandMethodBuilder> consumer) {
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
    public CommandBuilder configureCommandClass(String className, Consumer<InnerCommandBuilder> consumer) {
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
        return super.getKeys() == null ? handleBuilders.stream().map(CommandHandleBuilder::getKeys).flatMap(Arrays::stream).toArray(String[]::new) : super.getKeys();
    }

    @Override
    public CommandBuilder putProperty(Object property) {
        super.putProperty(property);
        return this;
    }

    @Override
    public <T> CommandBuilder putProperty(Class<? super T> type, T property) {
        super.putProperty(type, property);
        return this;
    }

    @Override
    public CommandBuilder setName(String name) {
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
        final String[] keys = super.getKeys();
        final List<CommandPreprocessor> preprocessorList = getPreprocessorList();
        final Supplier<Object> supplier;
        if (isPersistent) {
            if (obj != null) {
                supplier = () -> obj;
            } else {
                try {
                    final Object o = commandClass.newInstance();
                    supplier = () -> o;
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new CommandInitializationException("Could not instantiate Command object " + commandClass.getName(), e);
                }
            }
        } else {
            try {
                final MethodHandle constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class));
                supplier = () -> {
                    try {
                        return constructor.invoke();
                    } catch (Throwable throwable) {
                        LoggerFactory.getLogger("Command").error("Failed to instantiate CommandObject " + commandClass.getName(), throwable);
                        return null;
                    }
                };
            } catch (NoSuchMethodException e) {
                throw new CommandInitializationException("Class " + commandClass.getName() + " is missing a no-arg public constructor.");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return new CommandImpl(getName(), keys, supplier, handleMap, propertyMap, preprocessorList);
    }

    public CommandBuilder configure(Consumer<CommandBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    @Override
    public CommandBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        super.addPreprocessorFunction(identifier, function);
        return this;
    }

    @Override
    public CommandBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        super.addPreprocessorPredicate(identifier, predicate);
        return this;
    }

    @Override
    public CommandBuilder addPreprocessors(Iterable<CommandPreprocessor> preprocessors) {
        super.addPreprocessors(preprocessors);
        return this;
    }

    @Override
    public CommandBuilder sortPreprocessors() {
        super.sortPreprocessors();
        return this;
    }

    @Override
    public CommandBuilder addAssociatedPreprocessors() {
        super.addAssociatedPreprocessors();
        return this;
    }
}