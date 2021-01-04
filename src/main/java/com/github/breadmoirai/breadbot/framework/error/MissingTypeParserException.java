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

package com.github.breadmoirai.breadbot.framework.error;

import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CommandParameterBuilderImpl;

public class MissingTypeParserException extends CommandInitializationException {

    public MissingTypeParserException(CommandParameterBuilderImpl p) {
        super("Command Parameter: \"" + p.getParamName() + "\"'s parser has not been set for Command: \n" + p.getCommandBuilder());
    }

}