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

package com.github.breadmoirai.breadbot.framework.parameter;

public interface CommandParameter {

    default Object map(CommandParser parser) {
        return map(parser.getArgumentList(), parser);
    }

    Object map(CommandArgumentList list, CommandParser parser);

    Class<?> getType();

    int getFlags();

    int getIndex();

    int getWidth();

    boolean isMustBePresent();
}
