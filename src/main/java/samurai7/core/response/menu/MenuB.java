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

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import samurai7.waiter.EventWaiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class MenuB implements IMenu {

    List<Pair<Object, String>> options = new ArrayList<>();

    public MenuB addOption(String unicode, String option) {
        Objects.requireNonNull(unicode, "unicode emoji must not be null");
        options.add(new ImmutablePair<>(unicode, option));
        return this;
    }

    public MenuB addOption(Emote emote, String option) {
        Objects.requireNonNull(emote, "Emote must not be null");
        options.add(new ImmutablePair<>(emote, option));
        return this;
    }

    public MenuB setOnReaction(BiConsumer<GenericGuildMessageReactionEvent>)


    @Override
    public void attachOptions(EmbedBuilder embedBuilder) {
        embedBuilder.addField("", options.stream().map(pair -> {
            String s = pair.getRight();
            final Object left = pair.getLeft();
            if (left instanceof Emote) s = ((Emote) left).getAsMention() + " " + s;
            else s = left + " " + s;
            return s;
        }).collect(Collectors.joining("\n")), false);
    }

    @Override
    public void waitForEvent(ResponseMenu responseMenu, EventWaiter waiter) {

    }

    @Override
    public void addReactions(Message message) {
        options.stream().map(Pair::getLeft).forEach(o -> {
            if (o instanceof Emote) message.addReaction(((Emote) o)).queue();
            else message.addReaction(((String) o)).queue();
        });
    }

    @Override
    public void onDelete(ResponseMenu menu) {

    }
}
