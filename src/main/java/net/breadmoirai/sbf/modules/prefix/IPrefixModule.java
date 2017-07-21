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

import net.breadmoirai.sbf.core.IModule;
import org.json.JSONException;
import org.json.JSONObject;

public interface IPrefixModule extends IModule {

    @Override
    default String getName() {
        return "PrefixModule";
    }

    String getPrefix(long guildId);

    void changePrefix(long guildId, String newPrefix);

    @Override
    default boolean isJSONconfigurable() {
        return true;
    }

    @Override
    default void addJSONconfig(long guildId, JSONObject jsonObject) {
        jsonObject.put("prefix", getPrefix(guildId));
    }

    @Override
    default boolean loadJSONconfig(long guildId, JSONObject jsonObject) {
        if (!jsonObject.has("prefix")) return false;
        try {
            final String prefix = jsonObject.getString("prefix");
            if (prefix.isEmpty()) return false;
            if (!prefix.equals(getPrefix(guildId))) {
                changePrefix(guildId, prefix);
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}
