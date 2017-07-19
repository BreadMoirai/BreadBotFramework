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
import net.breadmoirai.sbf.core.IModule;
import net.breadmoirai.sbf.core.response.Responses;
import net.breadmoirai.sbf.core.response.Response;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Commands should be derived from either this or {@link BiModuleCommand}.
 *
 * <p><b>See example:</b>
 * <pre><code>
 * public class NowPlaying extends {@link ModuleCommand Command}{@literal <MusicModule>} {
 *   {@literal @}Override
 *    public {@link Response} execute(CommandEvent event, MusicModule module) {
 *        return {@link Responses#of Responses.of}(module.getNowPlaying());
 *    }
 * }</code></pre>
 *
 * @param <M> The Module of the command
 */
public abstract class ModuleCommand<M extends IModule> implements ICommand {

    private static Map<Class<? extends ModuleCommand>, Type> commandTypeMap = new HashMap<>();

    private M module;
    private CommandEvent event;

    @Override
    public final Optional<Response> call() {
        final Response r = execute(getEvent(), module);
        return Optional.ofNullable(r);
    }

    public abstract Response execute(CommandEvent event, M module);

    @Override
    final public CommandEvent getEvent() {
        return event;
    }

    @Override
    final public boolean setModules(Map<Type, IModule> moduleTypeMap) {
        Type moduleType = commandTypeMap.computeIfAbsent(this.getClass(), k -> TypeUtils.getTypeArguments(this.getClass(), ModuleCommand.class).get(ModuleCommand.class.getTypeParameters()[0]));
        //noinspection unchecked
        this.module = (M) moduleTypeMap.get(moduleType);

        return module != null;
    }

    @Override
    final public void setEvent(CommandEvent event) {
        this.event = event;
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return this.getClass().isAnnotationPresent(annotation);
    }

    public Response getHelp(CommandEvent event) {
        return Responses.of("No help available here.");
    }
}
