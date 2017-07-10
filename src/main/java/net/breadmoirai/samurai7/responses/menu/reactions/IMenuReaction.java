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
package net.breadmoirai.samurai7.responses.menu.reactions;

import net.breadmoirai.samurai7.responses.menu.ResponseMenu;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;

public interface IMenuReaction {

    boolean matches(GenericGuildMessageReactionEvent event);

    boolean hasOption();

    void addReactionTo(Message message);

    boolean hasPredicate();

    boolean apply(GenericGuildMessageReactionEvent event, ResponseMenu menu);

    String getDisplay();
}
