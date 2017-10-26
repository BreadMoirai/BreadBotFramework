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
package com.github.breadmoirai.breadbot.modules.source;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.CommandEngineBuilder;
import com.github.breadmoirai.breadbot.framework.CommandModule;
import com.github.breadmoirai.breadbot.framework.BreadBotClientBuilder;

public class SourceModule implements CommandModule {

    private final long sourceGuildId;

    public SourceModule(long sourceGuildId) {
        this.sourceGuildId = sourceGuildId;
    }

    @Override
    public void init(CommandEngineBuilder config, BreadBotClient client) {
//        config.addPostProcessPredicate(command -> {
//            if (command.isMarkedWith(SourceGuild.class)) {
//                long value = command.getClass().getAnnotation(SourceGuild.class).value();
//                if (value == 0) value = sourceGuildId;
//                return value == command.getEvent().getGuildId();
//            }
//            return true;
//        });
    }

    @Override
    public void initialize(BreadBotClientBuilder builder) {

    }
}
