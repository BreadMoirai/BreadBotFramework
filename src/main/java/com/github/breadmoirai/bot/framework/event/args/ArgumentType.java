package com.github.breadmoirai.bot.framework.event.args;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ArgumentType<T> extends Predicate<CommandArgument>, Function<CommandArgument, T> {

}
