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
import com.github.breadmoirai.bot.framework.util.TypeFinder;
import net.dv8tion.jda.core.utils.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;

public abstract class ModuleMultiCommand<M extends IModule> extends ModuleCommand<M> {

    @Override
    public void execute(CommandEvent event, M module) {
        Commands.getHandle(event.getKey().toLowerCase()).ifPresent(cmd -> {
            try {
                cmd.invoke(this, event, module);
            } catch (Throwable throwable) {
                Commands.LOG.fatal(throwable);
            }
        });
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return super.isMarkedWith(annotation) || (Commands.isAnnotatedWith(getEvent().getKey().toLowerCase(), annotation));
    }

    public static String[] register(Class<? extends ModuleMultiCommand> commandClass) {
        final Type moduleType = TypeFinder.getTypeArguments(commandClass.getClass(), BiModuleCommand.class)[0];
        return Arrays.stream(commandClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Key.class))
                .filter(method -> method.getReturnType() == Void.TYPE)
                .filter(method -> method.getParameterCount() == 2)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.getParameterTypes()[1] == moduleType)
                .flatMap(method -> Commands.mapMethodKeys(commandClass, method))
                .toArray(String[]::new);
    }

}
