package com.github.breadmoirai.breadbot.framework.error;

public class MissingCommandPluginException extends BreadBotException {

    public MissingCommandPluginException(String name) {
        super("A CommandPlugin was not found but is required: " + name);
    }
}
