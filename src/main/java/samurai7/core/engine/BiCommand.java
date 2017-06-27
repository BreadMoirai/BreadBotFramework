/*
 *       Copyright 2017 Ton Ly
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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Optional;

public abstract class BiCommand<M1 extends IModule, M2 extends IModule> implements ICommand {

    private static Type moduleType1, moduleType2;

    private M1 module1;
    private M2 module2;
    private CommandEvent event;

    @Override
    public final Optional<Response> call() {
        final Response r = execute(getEvent(), module1, module2);
        //noinspection Duplicates
        if (r != null) {
            if (r.getAuthorId() == 0) r.setAuthorId(getEvent().getAuthorId());
            if (r.getChannelId() == 0) r.setChannelId(getEvent().getChannelId());
            if (r.getGuildId() == 0) r.setGuildId(getEvent().getGuildId());
        }
        return Optional.ofNullable(r);
    }

    protected abstract Response execute(CommandEvent event, M1 module1, M2 module2);

    @Override
    final public CommandEvent getEvent() {
        return event;
    }

    @Override
    final public void setModules(Map<Type, IModule> moduleTypeMap) {
        if (moduleType1 == null) {
            final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(this.getClass(), Command.class);
            final TypeVariable<Class<Command>>[] typeParameters = Command.class.getTypeParameters();
            moduleType1 = typeArguments.get(typeParameters[0]);
            moduleType2 = typeArguments.get(typeParameters[1]);
        }
        //noinspection unchecked
        this.module1 = (M1) moduleTypeMap.get(moduleType1);
        //noinspection unchecked
        this.module2 = (M2) moduleTypeMap.get(moduleType2);
    }

    @Override
    final public void setEvent(CommandEvent event) {
        this.event = event;
    }
}
