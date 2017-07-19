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
package net.breadmoirai.sbf.modules.prefix;

import net.breadmoirai.sbf.database.Database;
import net.breadmoirai.sbf.core.SamuraiClient;
import net.breadmoirai.sbf.core.impl.CommandEngineBuilder;

public class DefaultPrefixModule implements IPrefixModule {

    private final String defaultPrefix;

    public DefaultPrefixModule(String prefix) {
        this.defaultPrefix = prefix;
    }

    @Override
    public void init(CommandEngineBuilder engineBuilder, SamuraiClient client) {
        if (!Database.get().tableExists("GuildPrefix")) {
            Database.get().useHandle(handle -> handle.execute("CREATE TABLE GuildPrefix ( " +
                    "guild BIGINT PRIMARY KEY, " +
                    "prefix VARCHAR(20))"));
        }
        engineBuilder.registerCommand(PrefixCommand.class);
    }

    @Override
    public String getPrefix(long guildId) {
        return Database.get().withHandle(handle ->
                handle.select("SELECT prefix FROM GuildPrefix WHERE guild = ?", guildId)
                        .mapTo(String.class)
                        .findFirst()
                        .orElseGet(() -> {
                            handle.insert("INSERT INTO GuildPrefix VALUES (?, ?)", guildId, defaultPrefix);
                            return defaultPrefix;
                        }));
    }

    @Override
    public void changePrefix(long guildId, String newPrefix) {
        Database.get().useHandle(handle -> handle.update("UPDATE GuildPrefix SET prefix = ? WHERE guild = ?", newPrefix, guildId));
    }

}
