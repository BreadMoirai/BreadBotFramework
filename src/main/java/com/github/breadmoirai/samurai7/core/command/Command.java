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
import com.github.breadmoirai.samurai7.core.response.Responses;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * This is a plain Command that is not explicitly tied to any Modules.
 */
public abstract class Command implements ICommand {

    private CommandEvent event;

    @Override
    public final Optional<Response> call() {
        final Response r = execute(getEvent());
        //noinspection Duplicates
        if (r != null) {
            if (r.getAuthorId() == 0) r.setAuthorId(getEvent().getAuthorId());
            if (r.getChannelId() == 0) r.setChannelId(getEvent().getChannelId());
            if (r.getGuildId() == 0) r.setGuildId(getEvent().getGuildId());
        }
        return Optional.ofNullable(r);
    }

    public abstract Response execute(CommandEvent event);

    @Override
    final public CommandEvent getEvent() {
        return event;
    }

    @Override
    final public boolean setModules(Map<Type, IModule> moduleTypeMap) {
        return true;
    }

    @Override
    final public void setEvent(CommandEvent event) {
        this.event = event;
    }

    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return this.getClass().isAnnotationPresent(annotation);
    }

    public Response getHelp(CommandEvent event) {
        return Responses.of("No help available here.");
    }
}
