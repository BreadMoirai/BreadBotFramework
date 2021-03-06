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

package com.github.breadmoirai.breadbot.plugins.owner;

import net.dv8tion.jda.api.entities.Member;

import java.util.Arrays;

/**
 * Commands annotated with {@link Owner @Owner} will only activate if the id provided to the constructor matches the user who sent the command.
 */
public class StaticOwnerPlugin extends OwnerPlugin {

    private final long[] owners;

    public StaticOwnerPlugin(long[] owners) {
        this.owners = owners;
        Arrays.sort(owners);
    }

    @Override
    public boolean isOwner(Member member) {
        return Arrays.binarySearch(owners, member.getUser().getIdLong()) >= 0;
    }

}