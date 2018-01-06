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
package com.github.breadmoirai.breadbot.framework.response.internal;

import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import com.github.breadmoirai.breadbot.framework.response.CommandResponseManager;

import java.util.ArrayDeque;
import java.util.Queue;

public class CommandResponseManagerImpl implements CommandResponseManager {

    private final Queue<CommandResponse> responses = new ArrayDeque<>();

    @Override
    public void accept(CommandResponse response) {
        responses.add(response);
    }

    @Override
    public void complete() {
        while (!responses.isEmpty()) {
            final CommandResponse poll = responses.poll();
            poll.dispatch(value -> {
            });
        }
    }
}
