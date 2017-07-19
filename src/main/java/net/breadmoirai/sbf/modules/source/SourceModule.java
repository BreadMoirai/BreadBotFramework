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
package net.breadmoirai.sbf.modules.source;

import net.breadmoirai.sbf.core.SamuraiClient;
import net.breadmoirai.sbf.core.impl.CommandEngineBuilder;
import net.breadmoirai.sbf.core.IModule;

public class SourceModule implements IModule {

    private final long sourceGuildId;

    public SourceModule(long sourceGuildId) {
        this.sourceGuildId = sourceGuildId;
    }

    @Override
    public void init(CommandEngineBuilder config, SamuraiClient client) {
        config.addPostProcessPredicate(command -> {
            if (command.isMarkedWith(SourceGuild.class)) {
                long value = command.getClass().getAnnotation(SourceGuild.class).value();
                if (value == 0) value = sourceGuildId;
                return value == command.getEvent().getGuildId();
            }
            return true;
        });
    }
}
