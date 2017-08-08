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

import com.github.breadmoirai.framework.event.CommandEvent;
import com.github.breadmoirai.framework.core.IModule;
import com.github.breadmoirai.framework.core.SamuraiClient;
import com.github.breadmoirai.framework.util.TypeFinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class BiModuleCommand<M1 extends IModule, M2 extends IModule> implements ICommand {

    private static Map<Class<? extends BiModuleCommand>, Type[]> commandTypeMap = new HashMap<>();

    private CommandEvent event;

    @Override
    public void run() {
        Type[] moduleType = commandTypeMap.computeIfAbsent(this.getClass(), k -> TypeFinder.getTypeArguments(this.getClass(), BiModuleCommand.class));

        final SamuraiClient client = getEvent().getClient();
        //noinspection unchecked
        execute(getEvent(), (M1) client.getModule(moduleType[0]), (M2) client.getModule(moduleType[1]));
    }

    public abstract void execute(CommandEvent event, M1 module1, M2 module2);

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return this.getClass().isAnnotationPresent(annotation);
    }

    @Override
    final public CommandEvent getEvent() {
        return event;
    }

    @Override
    final public void setEvent(CommandEvent event) {
        this.event = event;
    }
}
