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

public abstract class MultiCommand extends Command {

    private static final HashMap<Class<? extends MultiCommand>, HashMap<String, java.lang.reflect.Method>> METHOD_MAP = new HashMap<>();

    @Override
    public void execute(CommandEvent event) {
        final java.lang.reflect.Method method = METHOD_MAP.get(this.getClass()).get(event.getKey().toLowerCase());
        try {
            method.invoke(this, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return super.isMarkedWith(annotation) || METHOD_MAP.get(this.getClass()).get(getEvent().getKey().toLowerCase()).isAnnotationPresent(annotation);
    }

    public static String[] register(Class<? extends MultiCommand> commandClass) {
        final HashMap<String, java.lang.reflect.Method> map = new HashMap<>();
        METHOD_MAP.put(commandClass, map);
        return Arrays.stream(commandClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Key.class))
                .filter(method -> method.getReturnType() == Void.TYPE)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .flatMap(method -> Arrays.stream(method.getAnnotation(Key.class).value())
                        .peek(s -> map.put(s, method)))
                .toArray(String[]::new);
    }


}
