/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.plugins.prefix;

import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotBuilder;
import net.dv8tion.jda.api.entities.Guild;

/**
 * This is a mandatory module. This will be included by the BreadBotClientBuilder if it is not present
 */
public interface PrefixPlugin extends CommandPlugin {

    @Override
    default void initialize(BreadBotBuilder client) {
        client.addCommand(PrefixCommand.class);
    }

    String getPrefix(Guild guild);
}