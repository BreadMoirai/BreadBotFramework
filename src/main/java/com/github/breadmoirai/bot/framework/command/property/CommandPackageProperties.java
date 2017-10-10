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
package com.github.breadmoirai.bot.framework.command.property;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class CommandPackageProperties {

    /**
     * A private constructor for a static class.
     */
    private CommandPackageProperties() {
    }

    //packages

    private static Map<Package, CommandPropertyMap> packageMap = new HashMap<>();

    public static CommandPropertyMap getPropertiesForPackage(Package p) {
        if (p == null) return null;
        return packageMap.computeIfAbsent(p, CommandPackageProperties::createPropertiesForPackage);
    }

    private static CommandPropertyMap createPropertiesForPackage(Package p) {
        final CommandPropertyMapBuilder map = new CommandPropertyMapBuilder();
        for (Annotation a : p.getAnnotations()) {
            map.putProperty(a);
        }
        final String name = p.getName();
        final int i = name.lastIndexOf('.');
        if (i != -1) {
            final String parentPackageName = name.substring(0, i);
            final Package aPackage = Package.getPackage(parentPackageName);
            if (aPackage != null)
                map.setDefaultProperties(getPropertiesForPackage(aPackage));
        }
        return map.build();
    }

}
