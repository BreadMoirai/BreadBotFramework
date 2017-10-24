package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.command.CommandHandle;

public abstract class CommandHandleBuilderInternal implements CommandHandleBuilder {

    abstract CommandHandle build();

}