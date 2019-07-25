package com.github.breadmoirai.tests.commands;

import com.github.breadmoirai.breadbot.framework.annotation.command.Command;

public class SSICommand {

    @Command
    public String ssi(String a, String b, Integer c) {
        return a + ", " + b + ", " + c;
    }

    @Command
    public String sis(String a, Integer b, String c) {
        return a + ", " + b + ", " + c;
    }

    @Command
    public String iss(Integer a, String b, String c) {
        return a + ", " + b + ", " + c;
    }
}
