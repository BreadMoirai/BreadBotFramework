package com.github.breadmoirai.breadbot.framework.event;

import java.util.Iterator;

public interface ArgumentSplitter {

    Iterator<String> getArguments(String content);

}
