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
package com.github.breadmoirai.breadbot.modules.admin;

import com.github.breadmoirai.breadbot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.CommandModule;
import net.dv8tion.jda.core.entities.Member;

public interface AdminModule extends CommandModule {

    @Override
    default String getName() {
        return "AdminModule";
    }

    boolean isAdmin(Member member);

    @Override
    default void initialize(BreadBotClientBuilder builder) {
        builder.associatePreprocessorPredicate("admin", Admin.class, event -> isAdmin(event.getMember()));
        /* equivalent to
        builder.associatePreprocessor("admin", Admin.class, (commandObj, targetHandle, event, processorStack) -> {
            if (isAdmin(event.getMember())) {
                processorStack.runNext();
            }
        });
        */
    }
}
