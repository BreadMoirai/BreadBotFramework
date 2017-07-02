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
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class MenuStrategy121 {

    List<Object> reactions = new ArrayList<>();
    List<String> options = new ArrayList<>();
    List<Pair<Predicate<GenericMessageReactionEvent>, BiConsumer<GenericMessageReactionEvent, ResponseMenu>>> onReaction = new ArrayList<>();

    public MenuStrategy121 addOption(String unicode, String option, BiConsumer<GenericMessageReactionEvent, ResponseMenu> onSelection) {
        Objects.requireNonNull(unicode, "unicode emoji must not be null");
        Objects.requireNonNull(onSelection, "onSelection must not be null");
        reactions.add(unicode);
        options.add(option);
        onReaction.add(new ImmutablePair<>(evt -> evt.getReaction().getEmote().getName().equals(unicode), onSelection));
        return this;
    }

    public MenuStrategy121 addOption(Emote emote, String option, BiConsumer<GenericMessageReactionEvent, ResponseMenu> onSelection) {
        Objects.requireNonNull(emote, "Emote must not be null");
        Objects.requireNonNull(onSelection, "onSelection must not be null");
        reactions.add(emote);
        options.add(option);
        onReaction.add(new ImmutablePair<>(evt -> emote.equals(evt.getReactionEmote().getEmote()), onSelection));
        return this;
    }

    List<Object> getReactions() {
        return reactions;
    }

    List<String> getOptions() {
        return options;
    }

    List<Pair<Predicate<GenericMessageReactionEvent>, BiConsumer<GenericMessageReactionEvent, ResponseMenu>>> getOnReaction() {
        return onReaction;
    }
}
