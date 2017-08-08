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
 */
package com.github.breadmoirai.bot.framework.core.command;

import com.github.breadmoirai.bot.framework.core.CommandEvent;
import com.github.breadmoirai.bot.framework.core.IModule;
import com.github.breadmoirai.bot.framework.core.Response;
import com.github.breadmoirai.bot.framework.util.TypeFinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public abstract class ModuleMultiSubCommand<M extends IModule> extends ModuleCommand<M> {

    @Override
    public void execute(CommandEvent event, M module) {
        Commands.invokeCommand(getKey(event), this, event, module);
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return super.isMarkedWith(annotation) || Commands.isAnnotatedWith(getKey(getEvent()), annotation);
    }

    public String getKey(CommandEvent event) {
        final List<String> args = event.getArgs();
        final String subKey = args.size() > 1 ? args.get(0).toLowerCase() : "";
        return subKey.isEmpty() ? event.getKey().toLowerCase() : event.getKey().toLowerCase() + ' ' + subKey;
    }

    public static String[] register(Class<? extends ModuleMultiSubCommand> commandClass) {
        if (!commandClass.isAnnotationPresent(Command.class)) return null;
        final Type moduleType = TypeFinder.getTypeArguments(commandClass.getClass(), ModuleCommand.class)[0];
        Arrays.stream(commandClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Command.class))
                .filter(method -> method.getReturnType() == Void.TYPE || Response.class.isAssignableFrom(method.getReturnType()))
                .filter(method -> method.getParameterCount() == 2)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.getParameterTypes()[1] == moduleType)
                .forEach(method -> Commands.mapMethodKeys(commandClass, method));
        return commandClass.getAnnotation(Command.class).value();
    }
}