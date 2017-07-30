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
package net.breadmoirai.sbf.core.response.menu;

import net.breadmoirai.sbf.core.response.CloseableResponse;
import net.breadmoirai.sbf.core.response.simple.EditResponse;
import net.dv8tion.jda.core.entities.Message;
import net.breadmoirai.sbf.core.impl.Response;
import net.breadmoirai.sbf.waiter.EventWaiter;

public class ResponseMenu extends Response implements CloseableResponse {

    private transient final Menu menu;
    private transient Message message;

    protected ResponseMenu(Menu menu) {
        this.menu = menu;
    }

    @Override
    public Message buildMessage() {
        return menu.getMessage();
    }

    @Override
    public final void onSend(Message message) {
        this.message = message;
        menu.addReactions(message);
        menu.waitForEvent(this, EventWaiter.get());
    }

    /**
     * replaces message with {@code newResponse}
     *
     * @param newResponse the response to be used to replace
     */
    @Override
    public EditResponse replaceWith(Response newResponse) {
        EditResponse replace = replace(this.getMessageId());
        replace.base(this);
        return replace;
    }

    /**
     * deletes the menu.
     */
    public void delete() {
        message.delete().queue();
    }

    @Override
    public void cancel(Response cancelMessage, boolean clearReactions) {
        if (clearReactions)
            message.clearReactions().queue();
         replaceWith(cancelMessage);
    }

}
