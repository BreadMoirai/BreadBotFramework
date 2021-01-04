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
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TextChannelArgument extends MentionArgument {

    private final TextChannel channel;

    public TextChannelArgument(CommandEvent event, String s, TextChannel textChannel) {
        super(event, s);
        this.channel = textChannel;
    }

    @Override
    public boolean isTextChannel() {
        return true;
    }

    @Override
    public boolean isValidTextChannel() {
        return true;
    }

    @Override
    public TextChannel getTextChannel() {
        return channel;
    }

    @Override
    public Optional<TextChannel> findTextChannel() {
        return Optional.of(channel);
    }

    @Override
    public List<TextChannel> searchTextChannels() {
        return Collections.singletonList(channel);
    }
}
