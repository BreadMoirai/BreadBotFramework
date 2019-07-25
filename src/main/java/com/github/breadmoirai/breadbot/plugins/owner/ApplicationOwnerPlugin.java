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

package com.github.breadmoirai.breadbot.plugins.owner;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

/**
 * Commands annotated with {@link com.github.breadmoirai.breadbot.plugins.owner.Owner @Owner} will only activate if the id provided to the constructor matches the user who sent the command.
 */
public class ApplicationOwnerPlugin extends OwnerPlugin implements EventListener {

    private long ownerId;

    @Override
    public boolean isOwner(Member member) {
        return member.getUser().getIdLong() == ownerId;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ReadyEvent) {
            ownerId = event.getJDA().asBot().getApplicationInfo().complete().getOwner().getIdLong();
            event.getJDA().removeEventListener(this);
        }
    }

    @SubscribeEvent
    public void onReadyEvent(ReadyEvent event) {
        ownerId = event.getJDA().asBot().getApplicationInfo().complete().getOwner().getIdLong();
        event.getJDA().removeEventListener(this);
    }


}