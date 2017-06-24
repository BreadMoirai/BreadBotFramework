/*
 *       Copyright 2017 Ton Ly
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
package samurai7.core.command;

import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import samurai7.core.IModule;
import samurai7.core.response.Response;
import samurai7.modules.prefix.PrefixModule;
import samurai7.util.DiscordPatterns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;

public class CommandEventProcessor {

    private JDA jda;

    private final List<Pair<IModule, Method>> methods;
    private final Map<Type, IModule> moduleTypeMap;
    private final List<IModule> modules;
    private final Map<String, Class<? extends ICommand>> commandMap;

    private final Predicate<Message> preProcessPredicate;
    private final Predicate<ICommand> postProcessPredicate;
    private EventWaiter eventWaiter;
    private PrefixModule prefixModule;


    public CommandEventProcessor(CommandProcessorConfiguration configuration, List<IModule> modules, PrefixModule prefixModule) {
        this.modules = modules;
        this.commandMap = configuration.getCommandMap();
        final Predicate<Message> prePredicate = configuration.getPreProcessPredicate();
        this.preProcessPredicate = prePredicate == null ? message -> true : prePredicate;
        final Predicate<ICommand> postPredicate = configuration.getPostProcessPredicate();
        this.postProcessPredicate = postPredicate == null ? command -> true : postPredicate;
        this.eventWaiter = new EventWaiter();
        this.prefixModule = prefixModule;
        final HashMap<Type, IModule> typeMap = new HashMap<>(modules.size());
        final ArrayList<Pair<IModule, Method>> methodList = new ArrayList<>();
        for (IModule module : modules) {
            typeMap.put(module.getClass(), module);
            for (Method m : module.getClass().getDeclaredMethods())
                if (m.isAnnotationPresent(SubscribeEvent.class)
                        && m.getParameterCount() == 1
                        && !Modifier.isStatic(m.getModifiers())
                        && m.getParameterTypes()[0] == CommandEvent.class)
                    methodList.add(new ImmutablePair<>(module, m));
        }
        this.moduleTypeMap = Collections.unmodifiableMap(typeMap);
        methods = Collections.unmodifiableList(methodList);
    }

    private void processEvent(CommandEvent event) {
        if (event == null) return;
        ICommand command = null;
        final String key = event.getKey().toLowerCase();
        try {
            final Class<? extends ICommand> aClass = commandMap.get(key);
            command = aClass == null ? null : aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (command == null) {
            for (IModule module : modules) {
                command = module.getCommand(key);
                if (command != null) break;
            }
        }
        if (command == null) return;
        command.setEvent(event);
        command.setModules(moduleTypeMap);

        if (postProcessPredicate.test(command)) {
            CompletableFuture.supplyAsync(command::call).thenAcceptAsync(response -> response.ifPresent(this::submit));
            CompletableFuture.runAsync(() -> fireCommandEvent(event));
        }
    }

    private void submit(Response response) {
        response.setEventWaiter(this.eventWaiter);
        final TextChannel textChannel = jda.getTextChannelById(response.getChannelId());
        if (textChannel == null) return;
        final Message message = response.getMessage();
        if (message == null) return;
        textChannel.sendMessage(message).queue(response::onSend);
    }

    private void fireCommandEvent(CommandEvent event) {
        eventWaiter.onEvent(event);
        for (Pair<IModule, Method> pair : methods) {
            final Method method = pair.getValue();
            try {
                method.setAccessible(true);
                method.invoke(pair.getKey(), event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private CommandEvent createEvent(GenericGuildMessageEvent event, Message message) {
        String prefix = prefixModule.getPrefix(event.getGuild().getIdLong());
        String contentRaw = message.getContentRaw();
        String key, content;
        final Matcher matcher = DiscordPatterns.USER_MENTION_PREFIX.matcher(contentRaw);
        if (matcher.matches()) {
            contentRaw = contentRaw.substring(matcher.end(1)).trim();
            final String[] split = DiscordPatterns.WHITE_SPACE.split(contentRaw, 2);
            key = split[0];
            content = split.length > 1 ? split[1].trim() : null;
            return new MessageReceivedCommandEvent(event, message, prefix, key, content);
        } else {
            if (contentRaw.startsWith(prefix)) {
                final String[] split = DiscordPatterns.WHITE_SPACE.split(contentRaw.substring(prefix.length()), 2);
                key = split[0];
                content = split.length > 1 ? split[1].trim() : null;
                return new MessageReceivedCommandEvent(event, message, prefix, key, content);
            }
            return null;
        }
    }

    @SubscribeEvent
    public void onReadyEvent(ReadyEvent event) {
        this.jda = event.getJDA();
    }


    @SubscribeEvent
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (preProcessPredicate.test(event.getMessage()))
            processEvent(createEvent(event, event.getMessage()));
    }

    @SubscribeEvent
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (preProcessPredicate.test(event.getMessage()))
            processEvent(createEvent(event, event.getMessage()));
    }
}
