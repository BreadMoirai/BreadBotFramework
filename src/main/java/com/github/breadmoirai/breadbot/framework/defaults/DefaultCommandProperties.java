package com.github.breadmoirai.breadbot.framework.defaults;

import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommands;
import com.github.breadmoirai.breadbot.framework.annotation.command.*;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.*;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandPropertiesManagerImpl;
import com.github.breadmoirai.breadbot.framework.internal.command.builder.CommandHandleBuilderInternal;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentTypePredicate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultCommandProperties {

    private static final Pattern COMMAND_PATTERN = Pattern.compile(".+(command|cmd)(s)?");

    public void initialize(CommandPropertiesManagerImpl cp) {
        cp.putCommandModifier(Name.class, (p, builder) -> builder.setName(p.value()));
        cp.putCommandModifier(Group.class, (p, builder) -> builder.setGroup(p.value()));
        cp.putCommandModifier(Description.class, (p, builder) -> builder.setDescription(p.value()));
        cp.putCommandModifier(RequiredParameters.class, (p, builder) -> {
            for (int i : p.value()) {
                builder.configureParameter(i, param -> param.setRequired(true));
            }
        });
        cp.putCommandModifier(Description.class, (p, builder) -> builder.setDescription(p.value()));
        cp.putCommandModifier(Delimiter.class, (p, builder) -> {
            builder.setSplitRegex(p.regex(), p.limit());
        });


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

        cp.appendCommandModifier(null, (nullO, builder) -> {
            Class<?> declaringClass = builder.getDeclaringClass();
            if (Consumer.class.isAssignableFrom(declaringClass))
                return;

            for (Method method : builder.getDeclaringClass().getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (!Modifier.isPublic(modifiers))
                    continue;
                if (method.getParameterCount() != 1)
                    continue;
                if (method.getParameters()[0].getType() != CommandHandleBuilder.class)
                    continue;
                if (!method.isAnnotationPresent(ConfigureCommands.class) && !method.isAnnotationPresent(ConfigureCommand.class))
                    continue;
                final Object o;
                Object declaringObject = builder.getDeclaringObject();
                if (declaringObject != null && !(declaringObject instanceof Consumer)) {
                    o = declaringObject;
                } else if (Modifier.isStatic(modifiers)) {
                    o = ((CommandHandleBuilderInternal) builder).getObjectFactory().get();
                } else {
                    o = null;
                }
                ConfigureCommands annotation = method.getAnnotation(ConfigureCommands.class);
                ConfigureCommand[] value;
                if (annotation != null) {
                    value = annotation.value();
                } else {
                    value = new ConfigureCommand[]{method.getAnnotation(ConfigureCommand.class)};
                }
                for (ConfigureCommand configureCommand : value) {
                    if (configureCommand.value().equals(builder.getName()) || configureCommand.value().isEmpty()) {
                        try {
                            method.invoke(o, builder);
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
        cp.putParameterModifier(AbsentArgumentHandler.class, (p, builder) -> builder.setOnAbsentArgument(p));
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
        cp.putParameterModifier(Contiguous.class, (p, builder) -> builder.setContiguous(p.value()));
        cp.putParameterModifier(Numeric.class, (p, builder) -> {
            if (!(builder instanceof Function)) {
                final ArgumentParser<?> parser = builder.getParser();
                if (parser.hasPredicate()) {
                    builder.setParser(parser.getPredicate().and((arg, flags) -> arg.isNumeric()), parser.getMapper());
                } else {
                    builder.setParser((arg, flags) -> arg.isNumeric(), parser.getMapper());
                }
            }
        });
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
