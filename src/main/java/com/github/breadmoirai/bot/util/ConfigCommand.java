/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.bot.util;

import com.github.breadmoirai.bot.framework.IModule;
import com.github.breadmoirai.bot.framework.command.Command;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import com.github.breadmoirai.bot.modules.admin.Admin;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Admin
@Command
public class ConfigCommand {

    @Command
    public void download(CommandEvent event) {
        final JSONObject jsonObject = new JSONObject();
        for (IModule module : event.getClient().getModules()) {
            if (module.isJSONConfigurable()) {
                module.addJSONConfig(event.getGuildId(), jsonObject);
            }
        }
        if (jsonObject.toString().isEmpty()) {
            event.reply("Am no a configurable.");
        }
        if (!event.requirePermission(Permission.MESSAGE_ATTACH_FILES)) {
            event.replyWith(new MissingPermissionResponse(event, Permission.MESSAGE_ATTACH_FILES));
        }

        try (final PipedInputStream in = new PipedInputStream();
             final Writer writer = new OutputStreamWriter(new PipedOutputStream(in), StandardCharsets.UTF_8)) {
            writer.write(jsonObject.toString());
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
            final InputStream inputStream = new URL(file.getUrl()).openStream();
            final InputStreamReader reader = new InputStreamReader(inputStream);
            final BufferedReader buf = new BufferedReader(reader);
            final String collect = buf.lines().collect(Collectors.joining("\n"));
            final JSONObject jsonObject = new JSONObject(collect);
            int success = 0;
            List<IModule> failed = new ArrayList<>();
            for (IModule module : event.getClient().getModules()) {
                if (module.loadJSONConfig(event.getGuildId(), jsonObject)) {
                    success++;
                } else {
                    failed.add(module);
                }
            }
            if (failed.isEmpty()) {
                event.reply("Successfully configured " + success + " modules.");
            } else {
                event.reply("Configuration failed for modules: " + failed.stream().map(IModule::getName).collect(Collectors.joining(", ")) +
                        "\nSuccessfully configured " + success + " other modules.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            event.reply("```diff\n-" + e.getMessage() + "```");
        }
    }
}
