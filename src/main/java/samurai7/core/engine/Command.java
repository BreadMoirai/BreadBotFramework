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
 *
 */
package samurai7.core.engine;

import org.apache.commons.lang3.reflect.TypeUtils;
import samurai7.core.IModule;
import samurai7.core.response.Response;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * Commands should be derived from either this or {@link samurai7.core.engine.BiCommand}.
 * <p>
 * <p><b>See example:</b>
 * <pre><code>
 * public class NowPlaying extends {@link samurai7.core.engine.Command Command}{@literal <MusicModule>} {
 *   {@literal @}Override
 *    public {@link Response} execute(CommandEvent event, MusicModule module) {
 *        return {@link samurai7.core.response.Responses#of Responses.of}(module.getNowPlaying());
 *    }
 * }</code></pre>
 *
 * @param <M>
 */
public abstract class Command<M extends IModule> implements ICommand {

    private static Type moduleType;

    private M module;
    private CommandEvent event;

    @Override
    public final Optional<Response> call() {
        final Response r = execute(getEvent(), module);
        //noinspection Duplicates
        if (r != null) {
            if (r.getAuthorId() == 0) r.setAuthorId(getEvent().getAuthorId());
            if (r.getChannelId() == 0) r.setChannelId(getEvent().getChannelId());
            if (r.getGuildId() == 0) r.setGuildId(getEvent().getGuildId());
        }
        return Optional.ofNullable(r);
    }

    protected abstract Response execute(CommandEvent event, M module);

    @Override
    final public CommandEvent getEvent() {
        return event;
    }

    @Override
    final public void setModules(Map<Type, IModule> moduleTypeMap) {
        if (moduleType == null)
            moduleType = TypeUtils.getTypeArguments(this.getClass(), Command.class).get(Command.class.getTypeParameters()[0]);

        //noinspection unchecked
        this.module = (M) moduleTypeMap.get(moduleType);

    }

    @Override
    final public void setEvent(CommandEvent event) {
        this.event = event;
    }
}
