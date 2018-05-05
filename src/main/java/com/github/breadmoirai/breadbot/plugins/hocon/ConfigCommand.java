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

package com.github.breadmoirai.breadbot.plugins.hocon;

import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.plugins.admin.Admin;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Admin
public class ConfigCommand {

    @MainCommand
    public String config(CommandEvent event) {
        return String.format("Use `%1$sconfig download` to get a your guild's configuration and `%1$sconfig upload` after you've modified the file to make changes to the guilds configuration", event.getPrefix());
    }

    @Command
    public void download(CommandEvent event) {
        final Map<String, Object> conf = new HashMap<>();

        for (CommandPlugin plugin : event.getClient().getPlugins()) {
            if (plugin instanceof HOCONConfigurable) {
                ((HOCONConfigurable) plugin).buildConfig(event.getGuild(), conf);
            }
        }
        if (conf.isEmpty()) {
            event.reply("Am no a configurable.");
            return;
        }
        if (!event.checkPermission(Permission.MESSAGE_ATTACH_FILES)) {
            event.reply("I need permission to attach files.");
        }

        try (final PipedInputStream in = new PipedInputStream();
             final Writer writer = new OutputStreamWriter(new PipedOutputStream(in), StandardCharsets.UTF_8)) {
            final Config config = ConfigFactory.parseMap(conf);
            writer.write(config.toString());
            final Message build = new MessageBuilder()
                    .append("Modify and upload with `").append(event.getPrefix()).append("config upload`").build();
            event.getChannel().sendFile(in, event.getGuildId() + ".txt", build).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Command
    public void upload(CommandEvent event, Message.Attachment file) {
        try {
            InputStream inputStream = file.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            Config config = ConfigFactory.parseReader(inputStreamReader);

            final Map<Boolean, List<CommandPlugin>> map = event
                    .getClient()
                    .getPlugins()
                    .stream()
                    .filter(HOCONConfigurable.class::isInstance)
                    .collect(Collectors.partitioningBy(plugin ->
                            ((HOCONConfigurable) plugin).loadConfig(event.getGuild(), config)));
            List<CommandPlugin> failed = map.get(false);
            int success = map.get(true).size();

            if (failed.isEmpty()) {
                event.send("Successfully configured " + success + " modules.");
            } else {
                event.sendFormat("Configuration failed for modules: %s" +
                                "\nSuccessfully configured %d other modules.",
                                 failed.stream()
                              .map(CommandPlugin::getClass)
                              .map(Class::getSimpleName)
                              .collect(Collectors.joining(", ")), success);
            }
        } catch (IOException e) {
            event.send("Failed to read file.");
        }
    }
}