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
package com.github.breadmoirai.bot.framework.core.response;

import com.github.breadmoirai.bot.framework.core.Response;
import com.github.breadmoirai.bot.framework.core.response.menu.PromptBuilder;
import com.github.breadmoirai.bot.framework.core.response.menu.ReactionMenuBuilder;
import com.github.breadmoirai.bot.framework.core.response.simple.*;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Responses {

    @Contract("null -> null")
    public static StringResponse of(String message) {
        if (message == null || message.isEmpty()) return null;
        return new StringResponse(message);
    }


    @Contract("null, _ -> null")
    public static StringResponse ofFormat(String format, Object... args) {
        if (format == null || format.isEmpty()) return null;
        return new StringResponse(String.format(format, args));
    }

    @Contract("null -> null; !null -> !null")
    public static EmbedResponse of(MessageEmbed embed) {
        if (embed == null) return null;
        return new EmbedResponse(embed);
    }

    @Contract("null -> null; !null -> !null")
    public static MessageResponse of(Message message) {
        if (message == null) return null;
        return new MessageResponse(message);
    }

    @Contract("_, null -> null; _, !null -> !null")
    public static EditResponse edit(long targetMessageId, Response edit) {
        if (edit == null) return null;
        return new EditResponse(edit, targetMessageId);
    }


    @NotNull
    public static ReactionResponse react(long targetMessageId, String unicode) {
        return new ReactionResponse(targetMessageId, unicode);
    }

    @NotNull
    public static ReactionResponse react(long targetMessageId, Emote emote) {
        return new ReactionResponse(targetMessageId, emote);
    }

    public static ReactionMenuBuilder newReactionMenu() {
        return new ReactionMenuBuilder();
    }

    public static PromptBuilder newPrompt() {
        return new PromptBuilder();
    }

//    public static <T> ListBuilder<T> newList() {
//        return new ListBuilder<>();
//    }
}