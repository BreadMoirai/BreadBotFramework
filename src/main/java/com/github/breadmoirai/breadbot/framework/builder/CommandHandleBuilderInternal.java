package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.command.CommandHandle;
import com.github.breadmoirai.breadbot.framework.command.impl.CommandPropertyMapImpl;

public interface CommandHandleBuilderInternal extends CommandHandleBuilder {

    CommandPropertyMapImpl getPropertyMap();

    CommandHandle build();

    void putCommandHandle(CommandHandleBuilderInternal handle);
}