package com.github.breadmoirai.breadbot.framework.parameter.internal.collections;

import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.ArgumentParserImpl;

public class ArgumentParserCollectionImpl implements ArgumentParser {

    private final ArgumentParserImpl baseParser = new ArgumentParserImpl();
    private final ArgumentCollectionBuilder collector;

    public ArgumentParserCollectionImpl(ArgumentCollectionBuilder collector) {
        this.collector = collector;
    }

    @Override
    public Object parse(CommandParameter parameter, CommandArgumentList list, CommandParser parser) {
        boolean contiguous = parameter.isContiguous();
        if (!contiguous) {
            Object o;
            while ((o = baseParser.parse(parameter, list, parser)) != null) {
                collector.accept(o);
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (parser.hasMappedArguments(i)) continue;
                int j = i;
                Object o;
                while (j < list.size() &&
                        !parser.hasMappedArguments(j) &&
                        (o = baseParser.parse(parameter, list.subList(j, j + 1), parser)) != null) {
                    collector.accept(o);
                    j++;
                }
                if (j > i) break;
            }
        }
        return collector.build();
    }

}
