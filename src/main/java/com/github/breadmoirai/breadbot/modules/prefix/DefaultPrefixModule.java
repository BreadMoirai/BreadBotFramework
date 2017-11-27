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

import net.dv8tion.jda.core.entities.Guild;

/**
 * Provides the same prefix for all guilds as it was specified in the constructor. Non-configurable.
 */
public class DefaultPrefixModule implements PrefixModule {

    private final String defaultPrefix;

    public DefaultPrefixModule(String prefix) {
        this.defaultPrefix = prefix;
    }

    @Override
    public String getPrefix(Guild guild) {
        return defaultPrefix;
    }

}
