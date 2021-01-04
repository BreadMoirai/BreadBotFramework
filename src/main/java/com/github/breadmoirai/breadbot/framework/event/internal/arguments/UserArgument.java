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

package com.github.breadmoirai.breadbot.framework.event.internal.arguments;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import net.dv8tion.jda.api.entities.User;

public class UserArgument extends MentionArgument {

    private final User user;

    public UserArgument(CommandEvent event, String arg, User user) {
        super(event, arg);
        this.user = user;
    }

    @Override
    public boolean isUser() {
        return true;
    }

    @Override
    public boolean isValidUser() {
        return true;
    }

    @Override
    public User getUser() {
        return user;
    }
}
