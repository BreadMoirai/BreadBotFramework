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
package net.breadmoirai.sbf.core;

import net.breadmoirai.sbf.core.response.Response;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Optional;

public interface SamuraiClient {

    boolean hasModule(String moduleName);

    boolean hasModule(Class<? extends IModule> moduleClass);

    <T extends IModule> T getModule(Class<T> moduleClass);

    Optional<IModule> getModule(String moduleName);

    CommandEngine getCommandEngine();

    void submit(Response response);

    void submit(long channeId, Response response);

    void submit(TextChannel channel, Response response);
}
