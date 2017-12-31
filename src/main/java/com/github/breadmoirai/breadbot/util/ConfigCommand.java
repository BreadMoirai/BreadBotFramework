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

package com.github.breadmoirai.breadbot.util;

import com.github.breadmoirai.breadbot.framework.CommandModule;
import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.modules.admin.Admin;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.dv8tion.jda.core.entities.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Admin
public class ConfigCommand {

    @Command
    public void download(CommandEvent event) {
//        final Map<String, Object> conf = new HashMap<>();
//        for (CommandModule module : event.getClient().getModules()) {
//            if (module.isConfigurable()) {
//                module.buildConfig(event.getGuild(), conf);
//            }
//        }
//        if (jsonObject.toString().isEmpty()) {
//            event.reply("Am no a configurable.");
//            return;
//        }
//        if (!event.checkPermission(Permission.MESSAGE_ATTACH_FILES)) {
//            event.reply("I need permission to attach files.");
//        }
//
//        try (final PipedInputStream in = new PipedInputStream();
//             final Writer writer = new OutputStreamWriter(new PipedOutputStream(in), StandardCharsets.UTF_8)) {
//            writer.write(jsonObject.toString());
//            final Message build = new MessageBuilder()
//                    .append("Modify and upload with `").append(event.getPrefix()).append("config upload`").build();
//            event.getChannel().sendFile(in, event.getGuildId() + ".txt", build).queue();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Command
    public void upload(CommandEvent event, Message.Attachment file) {
        try {
            InputStream inputStream = file.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            Config config = ConfigFactory.parseReader(inputStreamReader);
            int success = 0;
            List<CommandModule> failed = new ArrayList<>();
            Map<Boolean, List<CommandModule>> map = event.getClient().getModules().stream().filter(CommandModule::isConfigurable).collect(Collectors.partitioningBy(m -> m.loadConfig(event.getGuild(), config)));
            StringBuilder sb = new StringBuilder();
            event.reply("");
            event.reply("Successfully configured " + success + " modules.");

                event.reply("Configuration failed for modules: " + failed.stream().map(CommandModule::getName).collect(Collectors.joining(", ")) +
                        "\nSuccessfully configured " + success + " other modules.");

        } catch (IOException e) {
            event.reply("Failed to read file.");
        }
    }
}