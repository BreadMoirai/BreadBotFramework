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
package com.github.breadmoirai.breadbot.modules.prefix;

import com.github.breadmoirai.breadbot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.CommandModule;

/**
 * This is a mandatory module. This will be included by the BreadBotClientBuilder if it is not present
 */
public interface PrefixModule extends CommandModule {

    @Override
    default void initialize(BreadBotClientBuilder client) {
        client.createCommand(PrefixCommand.class);
    }

    @Override
    default String getName() {
        return "PrefixModule";
    }

    String getPrefix(long guildId);
}
