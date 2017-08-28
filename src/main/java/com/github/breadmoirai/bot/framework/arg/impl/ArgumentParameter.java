package com.github.breadmoirai.bot.framework.arg.impl;

import com.github.breadmoirai.bot.framework.arg.CommandArgumentList;

import java.util.function.Function;

public interface ArgumentParameter extends Function<CommandArgumentList, Object> {

}
