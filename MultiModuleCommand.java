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
package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.core.CommandEvent;
import com.github.breadmoirai.framework.core.IModule;
import com.github.breadmoirai.framework.util.TypeFinder;
import net.dv8tion.jda.core.utils.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Commands should be derived from either this or {@link BiModuleCommand}.
 *
 * <p><b>See example:</b>
 * <pre><code>
 * public class NowPlaying extends {@link MultiModuleCommand Command}{@literal <MusicModule>} {
 *   {@literal @}Override
 *    public void execute(CommandEvent event, MusicModule module) {
 *        event.reply(module.getNowPlaying());
 *    }
 * }</code></pre>
 *
 */
public abstract class MultiModuleCommand implements ICommand {

    private static Map<Class<? extends MultiModuleCommand>, Type> commandTypeMap = new HashMap<>();

    private IModule[] modules;
    private CommandEvent event;

    public void execute(CommandEvent event, IModule[] modules) {
        Commands.getHandle(event.getKey().toLowerCase()).ifPresent(cmd -> {
            try {
                cmd.invoke(this, event, modules);
            } catch (Throwable throwable) {
                Commands.LOG.fatal(throwable);
            }
        });
    }


    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return this.getClass().isAnnotationPresent(annotation) || (Commands.isAnnotatedWith(getEvent().getKey().toLowerCase(), annotation));
    }

    public static String[] register(Class<? extends ModuleMultiCommand> commandClass) {
        final Type moduleType = TypeFinder.getTypeArguments(commandClass.getClass(), BiModuleCommand.class)[0];
        final HashMap<String, Pair<MethodHandle, Annotation[]>> map = new HashMap<>();
        return Arrays.stream(commandClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Key.class))
                .filter(method -> method.getReturnType() == Void.TYPE)
                .filter(method -> method.getParameterCount() == 2)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.getParameterTypes()[1] == moduleType)
                .flatMap(method -> Commands.mapMethodKeys(commandClass, map, method))
                .toArray(String[]::new);
    }
}
