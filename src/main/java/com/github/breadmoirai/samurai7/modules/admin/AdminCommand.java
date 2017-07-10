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
package com.github.breadmoirai.samurai7.modules.admin;

import com.github.breadmoirai.samurai7.core.engine.CommandEvent;
import com.github.breadmoirai.samurai7.core.engine.Key;
import com.github.breadmoirai.samurai7.responses.Responses;
import com.github.breadmoirai.samurai7.core.engine.Command;
import net.dv8tion.jda.core.entities.Member;
import com.github.breadmoirai.samurai7.core.response.Response;

import java.util.stream.Collectors;

@Admin
@Key("admin")
public class AdminCommand extends Command<IAdminModule> {
    @Override
    public Response execute(CommandEvent event, IAdminModule module) {
        return Responses.of("**Administrative Members:** " + event.getGuild().getMembers().stream().filter(module::isAdmin).map(Member::getEffectiveName).collect(Collectors.joining(", ")));
    }
}
