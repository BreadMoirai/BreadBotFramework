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
package net.breadmoirai.sbf.core.command;

import net.breadmoirai.sbf.core.CommandEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class MultiSubCommand extends Command {

    private static final HashMap<Class<? extends MultiSubCommand>, HashMap<String, java.lang.reflect.Method>> METHOD_MAP = new HashMap<>();

    @Override
    public void execute(CommandEvent event) {
        final List<String> args = event.getArgs();
        final String subKey = args.size() > 1 ? args.get(0).toLowerCase() : "";
        final java.lang.reflect.Method method = METHOD_MAP.get(this.getClass()).get(subKey);
        try {
            method.invoke(this, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        final java.lang.reflect.Method method = METHOD_MAP.get(this.getClass()).get(getEvent().getKey().toLowerCase());
        return super.isMarkedWith(annotation) || (method != null && method.isAnnotationPresent(annotation));
    }

    public static String[] register(Class<? extends MultiSubCommand> commandClass) {
        if (!commandClass.isAnnotationPresent(Key.class)) return null;
        final HashMap<String, java.lang.reflect.Method> map = new HashMap<>();
        METHOD_MAP.put(commandClass, map);
        Arrays.stream(commandClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Key.class))
                .filter(method -> method.getReturnType() == Void.TYPE)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .forEach(method -> Arrays.stream(method.getAnnotation(Key.class).value())
                        .forEach(s -> map.put(s, method)));
        return commandClass.getAnnotation(Key.class).value();
    }
}