package com.github.breadmoirai.breadbot.framework.internal;

import com.github.breadmoirai.breadbot.framework.command.parameter.*;

public class DefaultCommandProperties {

    public void initialize(CommandPropertiesImpl cp) {
        cp.putParameterModifier(Flags.class, (p, builder) -> builder.setFlags(p.value()));
        cp.putParameterModifier(MissingArgumentHandler.class, (p, builder) -> builder.setOnParamNotFound(p));
        cp.putParameterModifier(Required.class, (p, builder) -> builder.setRequired(true));
        cp.putParameterModifier(Index.class, (p, builder) -> builder.setIndex(p.value()));
        cp.putParameterModifier(MatchRegex.class, (p, builder) -> {
            ArgumentParser<?> parser = builder.getParser();
            ArgumentTypePredicate predicate;
            if (parser.hasPredicate()) {
                predicate = (arg, flags) -> arg.matches(p.value()) && parser.test(arg, flags);
            }
            else {
                predicate = (arg, flags) -> arg.matches(p.value());
            }
            builder.setParser(predicate, parser.getMapper());
        });
        cp.putParameterModifier(Width.class, (p, builder) -> builder.setWidth(p.value()));
        cp.putParameterModifier(Type.class, (p, builder) -> builder.setBaseType(p.value()));
    }

}
