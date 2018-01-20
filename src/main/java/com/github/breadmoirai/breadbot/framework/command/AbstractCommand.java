package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.internal.CommandEventInternal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractCommand implements Command {

    protected String[] keys;
    protected String name, group, description;
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
        onCommand(event);
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
