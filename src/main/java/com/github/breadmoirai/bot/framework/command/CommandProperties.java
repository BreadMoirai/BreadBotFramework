/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.bot.framework.command;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class CommandProperties {

    /**
     * A private constructor for a static class.
     */
    private CommandProperties() {
    }

    private static Map<Package, CommandPropertyMap> packageMap = new HashMap<>();

    public static CommandPropertyMap getPropertiesForPackage(Package p) {
        return packageMap.computeIfAbsent(p, CommandProperties::createPropertiesForPackage);
    }

    private static CommandPropertyMap createPropertiesForPackage(Package p) {
        final CommandPropertyMapBuilder map = new CommandPropertyMapBuilder();
        for (Annotation a : p.getAnnotations()) {
            map.putProperty(a);
        }
        return map.build();
    }

    /**
     * Returns true if the map passed contains all the specified properties.
     *
     * @param map        a {@link com.github.breadmoirai.bot.framework.command.CommandPropertyMap}
     * @param properties a var-arg or array of Class<?> propertyTypes.
     * @return {@code true} if all the specified properties have a mapping in the map.
     */
    private static boolean hasProperties(CommandPropertyMap map, Class<?>... properties) {
        for (Class<?> property : properties) {
            if (!map.containsProperty(property))
                return false;
        }
        return true;
    }
}
