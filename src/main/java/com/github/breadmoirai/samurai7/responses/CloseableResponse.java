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
package com.github.breadmoirai.samurai7.responses;

import com.github.breadmoirai.samurai7.core.response.Response;
import com.github.breadmoirai.samurai7.responses.menu.ResponseMenu;

public interface CloseableResponse {

    /**
     * This method will replace message with specified response and clear reactions if param is set to true.
     *
     * @param cancelMessage  A Response to replace the menu with.
     * @param clearReactions this boolean indicates whether
     */
    void cancel(Response cancelMessage, boolean clearReactions);

    /**
     * Calls {@link ResponseMenu#cancel(Response, boolean) this#cancel} with param {@code "Action Cancelled", true}.
     */
    default void cancel() {
        cancel(Responses.of("Action Cancelled"), true);
    }

    /**
     * This will delete the response's message from Discord.
     */
    void delete();
}
