/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;

import java.util.Iterator;
import java.util.Map;

/**
 * This is used to invoke a command
 */
public interface Command {

    String[] getKeys();

    String getName();

    String getGroup();

    String getDescription();

    boolean handle(CommandEventInternal event, Iterator<String> keyItr);

    Map<String, Command> getChildren();

    Command getParent();

}