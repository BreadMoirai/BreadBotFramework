/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Do not use for help commands. Logic is not implemented.
 */
public abstract class AbstractCommand implements Command {

    private String[] keys;
    private String name, group, description;
    private Map<String, Command> children;
    private Command parent;

    public abstract void onCommand(CommandEvent event);

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean handle(CommandEventInternal event, Iterator<String> keyItr) {
        if (keyItr != null && keyItr.hasNext() && children != null) {
            final Command child = children.get(keyItr.next());
            if (child != null) if (child.handle(event, keyItr)) return true;
        }
        event.setCommand(this);
        onCommand(event);
        event.getManager().complete();
        return true;
    }

    @Override
    public Map<String, Command> getChildren() {
        return children;
    }

    @Override
    public Command getParent() {
        return parent;
    }

    protected void setParent(Command parent) {
        this.parent = parent;
    }

    protected void setKeys(String... keys) {
        this.keys = keys;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setGroup(String group) {
        this.group = group;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void addChild(AbstractCommand command) {
        if (children == null) {
            children = new HashMap<>();
        }
        for (String s : command.getKeys()) {
            children.put(s, command);
        }
        command.setParent(this);
    }

}
