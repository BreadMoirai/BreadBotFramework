/*
 *      Copyright 2017 Ton Ly (BreadMoirai)
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
 *
 */
package samurai7.core.response.menu;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import org.apache.http.util.Args;
import samurai7.core.response.menu.reactions.IMenuReaction;
import samurai7.core.response.menu.reactions.MenuEmoji;
import samurai7.core.response.menu.reactions.MenuEmote;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class ReactionMenuBuilder extends MenuBuilder {
    private List<IMenuReaction> reactions = new ArrayList<>();
    private BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> onReaction;

    public ReactionMenuBuilder addOption(String unicode, BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> onSelection) {
        return addOption(unicode, null, onSelection);
    }


    public ReactionMenuBuilder addOption(Emote emote, BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> onSelection) {
        return addOption(emote, null, onSelection);
    }

    public ReactionMenuBuilder addOption(String unicode) {
        return addOption(unicode, null, null);
    }

    public ReactionMenuBuilder addOption(Emote emote) {
        return addOption(emote, null, null);
    }

    public ReactionMenuBuilder addOption(String unicode, String option) {
        return addOption(unicode, option, null);
    }

    public ReactionMenuBuilder addOption(Emote emote, String option) {
        return addOption(emote, option, null);
    }

    public ReactionMenuBuilder addOption(String unicode, String option, BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> onSelection) {
        Args.notNull(unicode, "Unicode Emoji");
        Args.containsNoBlanks(unicode, "Unicode Emoji");
        reactions.add(new MenuEmoji(unicode, option, onSelection));
        return this;
    }

    public ReactionMenuBuilder addOption(Emote emote, String option, BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> onSelection) {
        Args.notNull(emote, "Emote");
        reactions.add(new MenuEmote(emote, option, onSelection));
        return this;
    }

    public ReactionMenuBuilder setOnReaction(BiPredicate<GenericGuildMessageReactionEvent, ResponseMenu> onReaction) {
        this.onReaction = onReaction;
        return this;
    }

    @Override
    protected Menu build() {
        return new ReactionMenu(reactions, onReaction);
    }
}