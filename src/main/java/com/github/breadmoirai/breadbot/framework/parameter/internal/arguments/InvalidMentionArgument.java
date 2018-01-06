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

package com.github.breadmoirai.breadbot.framework.parameter.internal.arguments;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

/**
 * If this argument is a mention but not a valid mention.
 * Only applies to User, Member, Role, and TextChannel mentions.
 * The invalid id can be retrieved with {@link InvalidMentionArgument#getInvalidId()};
 *
 */
public class InvalidMentionArgument extends MentionArgument {
    private final long id;

    public InvalidMentionArgument(CommandEvent event, String s, long id) {
        super(event, s);
        this.id = id;
    }

    /**
     * returns the long value found within this properly formatted mention.
     * @return a positive long value.
     */
    public long getInvalidId() {
        return id;
    }

}