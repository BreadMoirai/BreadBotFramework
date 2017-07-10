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
package com.github.breadmoirai.samurai7.modules.source;

import com.github.breadmoirai.samurai7.core.IModule;
import com.github.breadmoirai.samurai7.core.engine.CommandEngineConfiguration;

public class SourceModule implements IModule {

    private final long sourceGuildId;

    public SourceModule(long sourceGuildId) {
        this.sourceGuildId = sourceGuildId;
    }

    @Override
    public void init(CommandEngineConfiguration config) {
        config.addPostProcessPredicate(command -> !command.getClass().isAnnotationPresent(Source.class) || command.getEvent().getGuildId() == sourceGuildId);
    }
}
