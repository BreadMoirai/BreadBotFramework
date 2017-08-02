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
package com.github.breadmoirai.bot.framework.core;

import com.github.breadmoirai.bot.framework.core.impl.Response;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Optional;

public interface SamuraiClient {

    boolean hasModule(String moduleName);

    boolean hasModule(Class<? extends IModule> moduleClass);

    <T extends IModule> Optional<T> getModule(Class<T> moduleClass);

    Optional<IModule> getModule(String moduleName);

    CommandEngine getCommandEngine();

    void send(Response response);

    void send(long channeId, Response response);

    void send(TextChannel channel, Response response);

    void send(User user, Response response);
}
