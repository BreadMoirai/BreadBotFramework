package com.github.breadmoirai.bot.framework.error;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MissingCommandMethod extends Error {
    public MissingCommandMethod(Class<?> command) {
        super(String.format("CommandClass %s does not a compatible method declaration for execution",
                command.getName()));
    }
}
