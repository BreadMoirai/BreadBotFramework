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
package samurai7;import net.dv8tion.jda.core.exceptions.RateLimitedException;
import samurai7.SamuraiBuilder;

import javax.security.auth.login.LoginException;

public class SamuraiRunner {

    public static void main(String[] args) {
        try {
            new SamuraiBuilder()
                    .setDefaultPrefix("-")
                    .addDefaultAdminModule()
                    .allowModifiablePrefix(true)
                    .buildJDA()
                    .setToken("MzI4Mjc0OTk1NzE1MjQ0MDM0.DDDX_w.t5I3fhaYnJQtzYN3cImK_3XZ-Q4")
                    .buildAsync();
        } catch (LoginException | RateLimitedException e) {
            e.printStackTrace();
        }

    }
}

