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
import com.github.breadmoirai.framework.util.TypeFinder;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Commands should be derived from either this or {@link BiModuleCommand}.
 *
 * <p><b>See example:</b>
 * <pre><code>
 * public class NowPlaying extends {@link ModuleCommand Command}{@literal <MusicModule>} {
 *   {@literal @}Override
 *    public void execute(CommandEvent event, MusicModule module) {
 *        event.reply(module.getNowPlaying());
 *    }
 * }</code></pre>
 *
 * @param <M> The Module of the command
 */
public abstract class ModuleCommand<M extends IModule> implements ICommand {

    private static Map<Class<? extends ModuleCommand>, Class<? extends IModule>> commandTypeMap = new HashMap<>();

    private M module;
    private CommandEvent event;

    @Override
    public final void run() {
        Class<? extends IModule> moduleType = commandTypeMap.computeIfAbsent(this.getClass(), this::getType);
        //noinspection unchecked
        this.module = (M) event.getClient().getModule(moduleType);
        execute(getEvent(), module);
    }

    public abstract void execute(CommandEvent event, M module);

    @Override
    final public CommandEvent getEvent() {
        return event;
    }

    @Override
    final public void setEvent(CommandEvent event) {
        this.event = event;
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return this.getClass().isAnnotationPresent(annotation);
    }

    private Class<? extends IModule> getType(Class<? extends ModuleCommand> k) {
        //noinspection unchecked
        return (Class<? extends IModule>) TypeFinder.getTypeArguments(this.getClass(), ModuleCommand.class)[0];
    }
}
