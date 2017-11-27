package com.github.breadmoirai.tests.menu;

import com.github.breadmoirai.breadbot.framework.response.Responses;
import com.github.breadmoirai.breadbot.framework.response.menu.Menu;

import java.util.concurrent.CompletableFuture;

public class ConfirmCommand {

    private final CompletableFuture<Boolean> future;

    public ConfirmCommand() {
        future = new CompletableFuture<>();
    }

    public Menu prompt() {
        return Responses.newPrompt()
                .onYes("yes", menuResponse -> future.complete(true))
                .onNo("no", menuResponse -> future.complete(false))
                .buildResponse("prompt");
    }

    public void result() {

    }
}
