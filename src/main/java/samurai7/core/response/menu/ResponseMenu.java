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

import net.dv8tion.jda.core.entities.Message;
import samurai7.core.engine.Response;

public abstract class ResponseMenu extends Response {

    private transient Message message;

    @Override
    public final void onSend(Message message) {
        this.message = message;
    }













    /**
     * replaces message with {@code newResponse}
     *
     * @param newResponse the response to be used to replace
     */
    public void replaceWith(Response newResponse) {
        message.editMessage(newResponse.getMessage()).queue(newResponse::onSuccess);
    }

    /**
     * deletes the menu.
     */
    public void delete() {
        message.delete().queue();
    }

    /**
     * calls {@link samurai7.core.response.menu.ResponseMenu#cancel(String) this#cancel} with param {@code "Action Canceled"}.
     */
    public void cancel() {
        cancel("Action cancelled");
    }

    /**
     * convenience method to clear reactions and replace message with specified String.
     *
     * @param cancelMessage A String to replace the menu with.
     */
    public void cancel(String cancelMessage) {
        message.clearReactions().queue();
        message.editMessage(cancelMessage).queue();
    }

}
