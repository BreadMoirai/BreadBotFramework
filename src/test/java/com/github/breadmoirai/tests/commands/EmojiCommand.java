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

package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.HandleAbsentArgument;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Required;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.util.Emoji;

@HandleAbsentArgument(EmojiCommand.MissingEmojiAlert.class)
public class EmojiCommand {

    @MainCommand
    public void emoji(CommandEvent event, @Required Emoji e) {
        event.reply(e.getUrl());
    }

    @Command
    public void name(CommandEvent event, @Required Emoji e) {
        event.reply(e.name().replace("_", " "));
    }


    public static class MissingEmojiAlert implements AbsentArgumentHandler {

        @Override
        public void handle(CommandEvent commandEvent, CommandParameter commandParameter) {
            commandEvent.reply("missing parameter: " + commandParameter.getType().getSimpleName());
        }
    }
}
