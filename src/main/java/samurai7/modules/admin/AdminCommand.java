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
package samurai7.modules.admin;

import net.dv8tion.jda.core.entities.Member;
import samurai7.core.engine.Command;
import samurai7.core.engine.CommandEvent;
import samurai7.core.engine.Key;
import samurai7.core.response.Response;
import samurai7.core.response.Responses;

import java.util.stream.Collectors;

@Admin
@Key("admin")
public class AdminCommand extends Command<AdminModule> {
    @Override
    public Response execute(CommandEvent event, AdminModule module) {
        return Responses.of("**Administrative Members:** " + event.getGuild().getMembers().stream().filter(module::isAdmin).map(Member::getEffectiveName).collect(Collectors.joining(", ")));
    }
}
