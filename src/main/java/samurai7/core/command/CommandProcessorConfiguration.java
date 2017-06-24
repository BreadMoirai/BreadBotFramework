/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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
 *
 */
package samurai7.core.command;

import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.simple.SimpleLoggerConfiguration;
import samurai7.util.DuplicateCommandKeyError;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue", "unused", "WeakerAccess"})
public class CommandProcessorConfiguration {

    private static Logger logger = LoggerFactory.getLogger("Command");

    private Predicate<Message> preProcessPredicate;
    private Predicate<ICommand> postProcessPredicate;

    private Map<String, Class<? extends ICommand>> commandMap = new HashMap<>();


    public CommandProcessorConfiguration addPreProcessPredicate(Predicate<Message> predicate) {
        if (preProcessPredicate == null) {
            preProcessPredicate = predicate;
        } else preProcessPredicate = preProcessPredicate.and(predicate);
        return this;
    }

    public CommandProcessorConfiguration addPostProcessPredicate(Predicate<ICommand> predicate) {
        if (postProcessPredicate == null) {
            postProcessPredicate = predicate;
        } else postProcessPredicate = postProcessPredicate.and(predicate);
        return this;
    }

    public CommandProcessorConfiguration registerCommand(Class<? extends ICommand> commandClass, String... keys) {
        for (String key : keys) {
            if (key == null || keys.length == 0) {
                logger.error("No key found for " + commandClass.getSimpleName());
                continue;
            }
            final String key1 = key.toLowerCase();
            if (commandMap.containsKey(key1)) {
                final Class<? extends ICommand> existing = commandMap.get(key1);
                logger.error("Key \"" + key + "\" for Command " + commandClass.getSimpleName() + " in Module " + TypeUtils.getTypeArguments(commandClass, Command.class).get(Command.class.getTypeParameters()[0]).getTypeName() + " is already mapped to Command " + existing.getSimpleName() + " in Module " + TypeUtils.getTypeArguments(existing, Command.class).get(Command.class.getTypeParameters()[0]).getTypeName());
            } else {
                commandMap.put(key, commandClass);
                final String typeName = TypeUtils.getTypeArguments(commandClass, Command.class).get(Command.class.getTypeParameters()[0]).getTypeName();
                logger.debug("\"" + key + "\" mapped to " + commandClass.getSimpleName() + " in Module " + typeName.substring(typeName.lastIndexOf('.') + 1));
            }
        }
        return this;
    }

    public CommandProcessorConfiguration registerCommand(Class<? extends ICommand> commandClass) {
        if (commandClass.isAnnotationPresent(Key.class)) {
            final String[] keyArray = commandClass.getAnnotation(Key.class).value();
            registerCommand(commandClass, keyArray);
        } else {
            logger.error("No key found for " + commandClass.getSimpleName());
        }
        return this;
    }

    public CommandProcessorConfiguration registerCommand(String commandPackagePrefix) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(commandPackagePrefix)))
                .setUrls(ClasspathHelper.forPackage(commandPackagePrefix))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Key.class);
        for (Class<?> commandClass : classes) {
            if (ICommand.class.isAssignableFrom(commandClass)) {
                //noinspection unchecked
                registerCommand((Class<? extends ICommand>) commandClass);
            }
        }
        return this;
    }

    Map<String, Class<? extends ICommand>> getCommandMap() {
        return commandMap;
    }

    Predicate<Message> getPreProcessPredicate() {
        return preProcessPredicate;
    }

    Predicate<ICommand> getPostProcessPredicate() {
        return postProcessPredicate;
    }
}
