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
package com.github.breadmoirai.botframework.modules.prefix;

import com.github.breadmoirai.botframework.core.command.Command;
import com.github.breadmoirai.botframework.core.command.ModuleCommand;
import com.github.breadmoirai.botframework.event.CommandEvent;

@Command
public class PrefixCommand extends ModuleCommand<IPrefixModule> {

//    @Override
//    public void execute(CommandEvent event, DynamicPrefixModule module) {
//        if (event.hasContent()) {
//            final String content = event.getContent();
//            if (DiscordPatterns.WHITE_SPACE.matcher(content).find()) {
//                event.reply("New prefix must not contain spaces");
//                return;
//            } else if (content.length() > 16) {
//                event.reply("New prefix must not be greater than 16 characters");
//                return;
//            } else {
//                module.changePrefix(event.getGuildId(), content);
//                event.replyFormat("Prefix has been set to `%s`", content);
//                return;
//            }
//        }
//        event.replyFormat("The current prefix is `%s`", module.getPrefix(event.getGuildId()));
//    }

    @Override
    public void execute(CommandEvent event, IPrefixModule module) {
        event.replyFormat("The current prefix is `%s`", module.getPrefix(event.getGuildId()));
    }
}