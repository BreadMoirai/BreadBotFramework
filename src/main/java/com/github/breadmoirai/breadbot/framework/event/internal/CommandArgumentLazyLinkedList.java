package com.github.breadmoirai.breadbot.framework.event.internal;

import com.github.breadmoirai.breadbot.framework.event.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;

import java.util.ArrayList;
import java.util.Iterator;

public class CommandArgumentLazyLinkedList extends CommandArgumentList {

    private final Iterator<String> itr;
    private final ArrayList<CommandArgument> contents;
    private final CommandArgumentFactory factory;

    public CommandArgumentLazyLinkedList(Iterator<String> itr, CommandEvent event) {
        super(event);
        this.itr = itr;
        this.contents = new ArrayList<>(5);
        this.factory = new CommandArgumentFactory(event);
    }

    @Override
    public CommandArgument get(int index) {
        int i = contents.size();
        while (i < index && itr.hasNext()) {
            contents.add(factory.parse(itr.next()));
            i++;
        }
        return contents.get(index);
    }

    @Override
    public int size() {
        while (itr.hasNext()) {
            contents.add(factory.parse(itr.next()));
        }
        return contents.size();
    }

}
