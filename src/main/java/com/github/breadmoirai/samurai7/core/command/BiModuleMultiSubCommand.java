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
package com.github.breadmoirai.samurai7.core.command;

import com.github.breadmoirai.samurai7.core.CommandEvent;
import com.github.breadmoirai.samurai7.core.IModule;
import com.github.breadmoirai.samurai7.core.response.Response;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BiModuleMultiSubCommand<M1 extends IModule, M2 extends IModule> extends BiModuleCommand<M1, M2> {

    private static final HashMap<Class<? extends BiModuleMultiSubCommand>, HashMap<String, Method>> METHOD_MAP = new HashMap<>();

    @Override
    public final Response execute(CommandEvent event, M1 module1, M2 module2) {
        final List<String> args = event.getArgs();
        final String subKey = args.size() > 1 ? args.get(0).toLowerCase() : "";
        final Method method = METHOD_MAP.get(this.getClass()).get(subKey);
        try {
            return (Response) method.invoke(this, event, module1, module2);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return super.isMarkedWith(annotation) || METHOD_MAP.get(this.getClass()).get(getEvent().getKey().toLowerCase()).isAnnotationPresent(annotation);
    }

    public static String[] register(Class<? extends BiModuleMultiSubCommand> commandClass) {
        if (!commandClass.isAnnotationPresent(Key.class)) return null;
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(commandClass.getClass(), BiModuleCommand.class);
        final TypeVariable<Class<BiModuleCommand>>[] typeParameters = BiModuleCommand.class.getTypeParameters();
        final Type moduleType1 = typeArguments.get(typeParameters[0]);
        final Type moduleType2 = typeArguments.get(typeParameters[1]);
        final HashMap<String, Method> map = new HashMap<>();
        METHOD_MAP.put(commandClass, map);
        Arrays.stream(commandClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Key.class))
                .filter(method -> Response.class.isAssignableFrom(method.getReturnType()))
                .filter(method -> method.getParameterCount() == 2)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.getParameterTypes()[1] == moduleType1)
                .filter(method -> method.getParameterTypes()[2] == moduleType2)
                .forEach(method -> Arrays.stream(method.getAnnotation(Key.class).value())
                        .forEach(s -> map.put(s, method)));
        return commandClass.getAnnotation(Key.class).value();
    }


}
