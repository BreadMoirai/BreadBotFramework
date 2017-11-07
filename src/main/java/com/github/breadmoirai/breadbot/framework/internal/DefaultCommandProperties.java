package com.github.breadmoirai.breadbot.framework.internal;

import com.github.breadmoirai.breadbot.framework.command.*;
import com.github.breadmoirai.breadbot.framework.command.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.command.internal.ConfigureCommands;
import com.github.breadmoirai.breadbot.framework.command.parameter.*;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultCommandProperties {

    private static final Pattern COMMAND_PATTERN = Pattern.compile(".+(command|cmd)(s)?");

    public void initialize(CommandPropertiesImpl cp) {
        cp.putCommandModifier(Name.class, (p, builder) -> builder.setName(p.value()));
        cp.putCommandModifier(Group.class, (p, builder) -> builder.setGroup(p.value()));
        cp.putCommandModifier(Description.class, (p, builder) -> builder.setDescription(p.value()));
        cp.putCommandModifier(RequiredParameters.class, (p, builder) -> {
            for (int i : p.value()) {
                builder.configureParameter(i, param -> param.setRequired(true));
            }
        });
        cp.putCommandModifier(Description.class, (p, builder) -> builder.setDescription(p.value()));

        cp.putCommandModifier(null, (o, builder) -> {
            Class<?> declaringClass = builder.getDeclaringClass();
            if (Consumer.class.isAssignableFrom(declaringClass)) {
                setGroupToPackage(builder, declaringClass);
                return;
            }
            Method declaringMethod = builder.getDeclaringMethod();
            String simpleName;
            if (declaringMethod == null)
                simpleName = declaringClass.getSimpleName().toLowerCase();
            else
                simpleName = declaringMethod.getName().toLowerCase();
            Matcher matcher = COMMAND_PATTERN.matcher(simpleName);
            if (matcher.find())
                simpleName = simpleName.substring(0, matcher.start(1));
            if (builder.getName() == null)
                builder.setName(simpleName);
            if (builder.getKeys() == null)
                builder.setKeys(simpleName);
            setGroupToPackage(builder, declaringClass);
        });

        cp.appendCommandModifier(null, (o, builder) -> {
            Class<?> declaringClass = builder.getDeclaringClass();
            if (Consumer.class.isAssignableFrom(declaringClass))
                return;

            for (Method method : builder.getDeclaringClass().getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers))
                    return;
                if (method.getParameterCount() != 1)
                    return;
                if (method.getParameters()[0].getType() != CommandHandleBuilder.class)
                    return;
                if (!method.isAnnotationPresent(ConfigureCommands.class))
                    return;
                ConfigureCommands annotation = method.getAnnotation(ConfigureCommands.class);
                for (ConfigureCommand configureCommand : annotation.value()) {
                    if (configureCommand.value().equals(builder.getName())) {
                        try {
                            method.invoke(null, builder);
                            break;
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            String msg = String.format("An Error occurred when attempting to configure CommandHandleBuilder[%s] with method %s#%s",
                                    builder.getName(), method.getDeclaringClass().getName(), method.getName());
                            throw new BreadBotException(msg, e);
                        }
                    }
                }
            }
        });

        cp.putParameterModifier(Flags.class, (p, builder) -> builder.setFlags(p.value()));
        cp.putParameterModifier(MissingArgumentHandler.class, (p, builder) -> builder.setOnParamNotFound(p));
        cp.putParameterModifier(Required.class, (p, builder) -> builder.setRequired(true));
        cp.putParameterModifier(Index.class, (p, builder) -> builder.setIndex(p.value()));
        cp.putParameterModifier(MatchRegex.class, (p, builder) -> {
            ArgumentParser<?> parser = builder.getParser();
            ArgumentTypePredicate predicate;
            if (parser.hasPredicate()) {
                predicate = (arg, flags) -> arg.matches(p.value()) && parser.test(arg, flags);
            } else {
                predicate = (arg, flags) -> arg.matches(p.value());
            }
            builder.setParser(predicate, parser.getMapper());
        });
        cp.putParameterModifier(Width.class, (p, builder) -> builder.setWidth(p.value()));
        cp.putParameterModifier(Type.class, (p, builder) -> builder.setBaseType(p.value()));
    }

    private void setGroupToPackage(CommandHandleBuilder builder, Class<?> declaringClass) {
        if (declaringClass.getPackage() != null) {
            String[] packageNames = declaringClass.getPackage().getName().split("\\.");
            String packageName = packageNames[packageNames.length - 1];
            if (packageName.matches("(command|cmd)(s)?") && packageNames.length > 1) {
                packageName = packageNames[packageNames.length - 2];
            }
            if (builder.getGroup() == null)
                builder.setGroup(packageName);
        }
    }

}
