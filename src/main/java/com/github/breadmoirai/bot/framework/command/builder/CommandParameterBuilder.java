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

import com.github.breadmoirai.bot.framework.IModule;
import com.github.breadmoirai.bot.framework.command.CommandParameter;
import com.github.breadmoirai.bot.framework.command.CommandParameterFunctionImpl;
import com.github.breadmoirai.bot.framework.command.arg.ArgumentTypeMapper;
import com.github.breadmoirai.bot.framework.command.arg.ArgumentTypePredicate;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.lang.reflect.Parameter;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface CommandParameterBuilder {

    public static CommandParameterBuilder builder(Parameter parameter) {
        final Class<?> type = parameter.getType();
        if (type == CommandEvent.class) {
            return new CommandParameterBuilderSpecificImpl("This parameter of type CommandEvent is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent()));
        } else if (IModule.class.isAssignableFrom(type)) {
            return new CommandParameterBuilderSpecificImpl("This parameter of type " + type.getSimpleName() + " is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent().getClient().getModule(type)));
        } else {
            return new CommandParameterBuilderImpl(parameter);
        }
    }


    /**
     * Sets the name of this parameter. Primarily used for debugging.
     * If {@code -parameters} is not passed to javac, parameter names will not be included in the compiled code and thus appear as {@code arg0, arg1, arg2, ...}, otherwise it will appear as in your code.
     *
     * @param paramName the name of the parameter.
     * @return this obj.
     */
    CommandParameterBuilder setName(String paramName);

    /**
     * Sets the flags to be passed to the {@link ArgumentTypeMapper}
     *
     * @param flags
     */
    CommandParameterBuilder setFlags(int flags);

    /**
     * If the index is known, the parser will only attempt to map the argument at the specified position.
     * If number of arguments is less than the index  The default value of {@code -1}
     *
     * @param index
     *
     * @return
     */
    CommandParameterBuilderImpl setIndex(int index);

    /**
     * Sets the width of the argument, how many tokens the parse should consume.
     *
     * @param width
     * @return
     */
    CommandParameterBuilderImpl setWidth(int width);

    /**
     * Sets the intended base type of the method. \\todo make a wiki
     *
     * @param type the Class of the argument.
     */
    <T> CommandParameterBuilder setBaseType(Class<T> type);

    /**
     * Sets the {@link ArgumentTypeMapper} to be used in mapping the {@link CommandParameter}.
     * If an {@link ArgumentTypeMapper} is registered in {@link com.github.breadmoirai.bot.framework.command.arg.ArgumentTypes}, it will not be used.
     * The provided {@link ArgumentTypeMapper} will not be registered with {@link com.github.breadmoirai.bot.framework.command.arg.ArgumentTypes}.
     * It is generally recommended to prefer using different {@link com.github.breadmoirai.bot.framework.command.arg.ArgumentFlags flags} on custom types to indicate that the {@link com.github.breadmoirai.bot.framework.command.arg.CommandArgument} should be mapped differently.
     *
     * @param mapper a public class that implements {@link ArgumentTypeMapper} and contains a no-args public constructor.
     */
    default <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentTypeMapper<T> mapper) {
        return setBaseType(type, null, mapper);
    }

    <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper);

    /**
     * @param mustBePresent {@code true} if the argument must be present. Otherwise an error message will be sent to the user with the default error or
     */
    CommandParameterBuilder setOptional(boolean mustBePresent);

    CommandParameterBuilder setOnParamNotFound(BiConsumer<CommandEvent, CommandParameter> onParamNotFound);

    CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator);

    CommandParameter build();
}
