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
package com.github.breadmoirai.bot.modules.admin;

import com.github.breadmoirai.bot.framework.CommandClient;
import com.github.breadmoirai.bot.framework.CommandEngineBuilder;
import com.github.breadmoirai.bot.framework.IModule;
import net.dv8tion.jda.core.entities.Member;

public interface IAdminModule extends IModule {

    @Override
    default String getName() {
        return "AdminModule";
    }

    @Override
    default void init(CommandEngineBuilder config, CommandClient client) {
//        config.addPostProcessPredicate(command -> !command.isMarkedWith(Admin.class) || isAdmin(command.getEvent().getMember()));
        config.registerCommand(AdminCommand.class);
    }

    boolean isAdmin(Member member);
}
