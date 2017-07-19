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
package net.breadmoirai.sbf.core.response;

import net.breadmoirai.sbf.core.response.menu.PromptBuilder;
import net.breadmoirai.sbf.core.response.menu.ReactionMenuBuilder;
import net.breadmoirai.sbf.core.response.simple.BasicResponse;
import net.breadmoirai.sbf.core.response.simple.EditResponse;
import net.breadmoirai.sbf.core.response.simple.ReactionResponse;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Responses {

    @Contract("null -> null")
    public static BasicResponse of(String message) {
        if (message == null || message.isEmpty()) return null;
        return new BasicResponse(new MessageBuilder().append(message).build());
    }


    @Contract("null, _ -> null")
    public static BasicResponse ofFormat(String format, Object... args) {
        if (format == null || format.isEmpty()) return null;
        return new BasicResponse(new MessageBuilder().appendFormat(format, args).build());
    }

    @Contract("null -> null; !null -> !null")
    public static BasicResponse of(MessageEmbed embed) {
        if (embed == null) return null;
        return new BasicResponse(new MessageBuilder().setEmbed(embed).build());
    }

    @Contract("null -> null; !null -> !null")
    public static BasicResponse of(Message message) {
        if (message == null) return null;
        return new BasicResponse(message);
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
