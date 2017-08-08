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

import java.lang.annotation.Annotation;

/**
 * This is a plain Command that is not explicitly tied to any Modules.
 */
public abstract class BasicCommand implements ICommand {

    private CommandEvent event;

    @Override
    public void run() {
        execute(event);
    }

    public abstract void execute(CommandEvent event);

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
}
