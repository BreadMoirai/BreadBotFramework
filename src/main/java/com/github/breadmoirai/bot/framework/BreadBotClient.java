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
package com.github.breadmoirai.bot.framework;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.IEventManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public interface BreadBotClient {

    boolean hasModule(String moduleName);

    boolean hasModule(Class<? extends ICommandModule> moduleClass);

    <T extends ICommandModule> T getModule(Class<T> moduleClass);

    ICommandModule getModule(String moduleName);

    ICommandModule getModule(Type moduleType);

    List<ICommandModule> getModules();

    CommandEngine getCommandEngine();

    JDA getJDA();

    IEventManager getEventManager();

    default void send(Response response) {
        send(response.getChannelId(), response);
    }

    default void send(long channeId, Response response) {
        TextChannel textChannel = getJDA().getTextChannelById(channeId);
        if (textChannel == null) return;
        send(textChannel, response);
    }


    default void send(TextChannel textChannel, Response response) {
        Objects.requireNonNull(textChannel, "TextChannel");
        Objects.requireNonNull(response, "Response");
        response.base(0, textChannel.getIdLong(), textChannel.getGuild().getIdLong(), 0, this);
        response.send(textChannel);
    }

    default void send(User user, Response response) {
        Objects.requireNonNull(user, "User");
        Objects.requireNonNull(user, "Response");
        response.setClient(this);
        user.openPrivateChannel().queue(response::send);
    }
}
