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
package com.github.breadmoirai.framework.core.command;

import com.github.breadmoirai.framework.core.IModule;
import com.github.breadmoirai.framework.core.Response;
import com.github.breadmoirai.framework.core.SamuraiClient;
import com.github.breadmoirai.framework.event.CommandEvent;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommandMethodHandle implements CommandHandle {

    private final Method method;
    private final String[] keys;
    private MethodHandle handle;
    private List<Class<? extends IModule>> params;


    public CommandMethodHandle(Method method) throws IllegalAccessException {
        this.method = method;
        handle = MethodHandles.publicLookup().unreflect(method);
        final Command annotation = method.getAnnotation(Command.class);
        final String[] value = annotation.value();
        if (value.length == 0)
        keys = new String[]{method.getName()};
        else keys = value;
    }

    public MethodHandle getHandle() {
        return handle;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return method.isAnnotationPresent(annotation);
    }

    @Override
    public boolean execute(Object parent, CommandEvent event, int subKey) throws Throwable {
        if (params == null) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final ArrayList<Class<? extends IModule>> params = new ArrayList<>(parameterTypes.length);
            this.params = params;
            for (int i = 1; i < parameterTypes.length; i++) {
                if (IModule.class.isAssignableFrom(parameterTypes[i])) {
                    //noinspection unchecked
                    params.add((Class<? extends IModule>) parameterTypes[i]);
                } else {
                    params.add(null);
                }
            }
        }
        final SamuraiClient client = event.getClient();
        final Object[] modules = params.stream().map(client::getModule).toArray();
        final Object invoke = getHandle().asSpreader(Object.class, modules.length).invoke(parent, event, modules);
        if (invoke instanceof Response){
            final Response r = (Response) invoke;
            r.base(event);
            r.send();
        }
        return true;
    }
}
