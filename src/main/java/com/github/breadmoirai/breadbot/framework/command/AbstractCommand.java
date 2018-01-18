package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;

import java.util.Iterator;
import java.util.Map;

public abstract class AbstractCommand implements Command {

    protected String[] keys;
    protected String name, group, description;

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
        onCommand(event);
        return true;
    }

    @Override
    public Map<String, Command> getChildren() {
        return null;
    }

    @Override
    public Command getParent() {
        return null;
    }

}
