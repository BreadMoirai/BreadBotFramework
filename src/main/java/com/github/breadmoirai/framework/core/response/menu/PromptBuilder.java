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
package com.github.breadmoirai.framework.core.response.menu;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class PromptBuilder extends MenuBuilder {


    private static String defaultYesEmoji = "\u2705", defaultNoEmoji = "\u274e";
    private static Emote defaultYesEmote, defaultNoEmote;
    private static final Consumer<ResponseMenu> DEFAULT_NO_CONSUMER = ResponseMenu::cancel;

    private static void setDefaultYesEmoji(String unicode) {
        defaultYesEmoji = unicode;
        defaultYesEmote = null;
    }

    private static void setDefaultYesEmote(Emote emote) {
        defaultYesEmote = emote;
        defaultYesEmoji = null;
    }

    private static void setDefaultNoEmoji(String unicode) {
        defaultNoEmoji = unicode;
        defaultNoEmote = null;
    }

    private static void setDefaultNoEmote(Emote emote) {
        defaultNoEmote = emote;
        defaultNoEmoji = null;
    }

    private Consumer<ResponseMenu> onYes, onNo;
    private String emojiYes, emojiNo;
    private Emote emoteYes, emoteNo;
    private String optionYes, optionNo;

    public PromptBuilder onYes(Consumer<ResponseMenu> onYes, String option) {
        this.onYes = onYes;
        this.optionYes = option;
        return this;
    }

    public PromptBuilder onNo(Consumer<ResponseMenu> onNo, String option) {
        this.onNo = onNo;
        this.optionYes = option;
        return this;
    }

    public PromptBuilder setYesReaction(String unicode) {
        this.emojiYes = unicode;
        this.emoteYes = null;
        return this;
    }

    public PromptBuilder setYesReaction(Emote emote) {
        this.emoteYes = emote;
        this.emojiYes = null;
        return this;
    }

    public PromptBuilder setNoReaction(String unicode) {
        this.emojiNo = unicode;
        this.emoteNo = null;
        return this;
    }

    public PromptBuilder setNoReaction(Emote emote) {
        this.emoteNo = emote;
        this.emojiNo = null;
        return this;
    }

    @Override
    public Menu build() {
        final ReactionMenuBuilder mb = new ReactionMenuBuilder();
        final BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> yesPredicate = (event, menu) -> {
            onYes.accept(menu);
            return true;
        };
        //noinspection Duplicates
        if (emoteYes != null) mb.addOption(emoteYes, optionYes, yesPredicate);
        else if (emojiYes != null) mb.addOption(emojiYes, optionYes, yesPredicate);
        else if (defaultYesEmote != null) mb.addOption(defaultYesEmote, optionYes, yesPredicate);
        else mb.addOption(defaultYesEmoji, optionYes, yesPredicate);

        final BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> noPredicate;
        if (onNo != null)
            noPredicate = (event, menu) -> {
                onNo.accept(menu);
                return true;
            };
        else
            noPredicate = (event, menu) -> {
                DEFAULT_NO_CONSUMER.accept(menu);
                return true;
            };
        //noinspection Duplicates
        if (emoteNo != null) mb.addOption(emoteNo, optionNo, noPredicate);
        else if (emojiNo != null) mb.addOption(emojiNo, optionNo, noPredicate);
        else if (defaultNoEmote != null) mb.addOption(defaultNoEmote, optionNo, noPredicate);
        else mb.addOption(defaultNoEmoji, optionNo, noPredicate);
        return mb.build();
    }
}
