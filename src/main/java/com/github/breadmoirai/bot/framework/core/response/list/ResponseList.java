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
package com.github.breadmoirai.bot.framework.core.response.list;

import com.github.breadmoirai.bot.framework.core.Response;
import com.github.breadmoirai.bot.framework.core.response.CloseableResponse;
import net.dv8tion.jda.core.entities.Message;

public class ResponseList extends Response implements CloseableResponse {

//    private transient final ListResponse;
    private transient Message message;

    @Override
    public Message buildMessage() {
        return null;
    }

    @Override
    public void onSend(Message message) {

    }

    @Override
    public void cancel(Response cancelMessage, boolean clearReactions) {

    }

    @Override
    public void delete() {

    }
}
