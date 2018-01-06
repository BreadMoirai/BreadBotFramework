/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.util;

import net.dv8tion.jda.client.managers.EmoteManager;
import net.dv8tion.jda.client.managers.EmoteManagerUpdatable;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import java.util.List;

public class EmojiEmote implements Emote {

    private final Emoji emoji;

    public EmojiEmote(Emoji emoji) {
        this.emoji = emoji;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    @Override
    public Guild getGuild() {
        return null;
    }

    @Override
    public List<Role> getRoles() {
        return null;
    }

    @Override
    public String getName() {
        String name = emoji.name().toLowerCase();
        if (name.charAt(0) == '_') return name.substring(1);
        return name;
    }

    @Override
    public boolean isManaged() {
        return false;
    }

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public AuditableRestAction<Void> delete() {
        return null;
    }

    @Override
    public EmoteManager getManager() {
        return null;
    }

    @Override
    public EmoteManagerUpdatable getManagerUpdatable() {
        return null;
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public String getImageUrl() {
        return emoji.getUrl();
    }

    @Override
    public String getAsMention() {
        return emoji.getUtf8();
    }

    @Override
    public boolean canInteract(Member issuer) {
        return false;
    }

    @Override
    public boolean canInteract(User issuer, MessageChannel channel) {
        return false;
    }

    @Override
    public boolean canInteract(User issuer, MessageChannel channel, boolean botOverride) {
        return false;
    }

    @Override
    public boolean isFake() {
        return true;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public long getIdLong() {
        return 0;
    }
}
