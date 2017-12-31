/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.breadmoirai.breadbot.framework.defaults;

import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommand;
import com.github.breadmoirai.breadbot.framework.annotation.ConfigureCommands;
import com.github.breadmoirai.breadbot.framework.annotation.Name;
import com.github.breadmoirai.breadbot.framework.annotation.command.Command;
import com.github.breadmoirai.breadbot.framework.annotation.command.Delimiter;
import com.github.breadmoirai.breadbot.framework.annotation.command.Description;
import com.github.breadmoirai.breadbot.framework.annotation.command.Group;
import com.github.breadmoirai.breadbot.framework.annotation.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.annotation.command.RequiredParameters;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Contiguous;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Flags;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Index;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.MatchRegex;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Required;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Width;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertiesManagerImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.builder.CommandHandleBuilderInternal;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParserFlags;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultCommandProperties {

    private static final Pattern COMMAND_PATTERN = Pattern.compile(".+(command|cmd)(s)?");

    public void initialize(CommandPropertiesManagerImpl cp) {
        cp.putCommandModifier(Command.class, (p, builder) -> {
            if (p.value().length != 0) builder.setKeys(p.value());
        });
        cp.putCommandModifier(MainCommand.class, (p, builder) -> {
            if (p.value().length != 0) builder.setKeys(p.value());
        });
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

        cp.putParameterModifier(Name.class, (p, builder) -> builder.setName(p.value()));
        cp.putParameterModifier(Flags.class, (p, builder) -> builder.setFlags(TypeParserFlags.get(p.value())));
        cp.putParameterModifier(AbsentArgumentHandler.class, (p, builder) -> builder.setOnAbsentArgument(p));
        cp.putParameterModifier(Required.class, (p, builder) -> builder.setRequired(true));
        cp.putParameterModifier(Index.class, (p, builder) -> builder.setIndex(p.value()));
        cp.putParameterModifier(MatchRegex.class, (p, builder) -> {
            TypeParser<?> parser = builder.getTypeParser();
            final Pattern pattern = Pattern.compile(p.value());
            builder.setTypeParser((arg, flags) -> {
                if (arg.matches(pattern)) {
                    return parser.parse(arg, flags);
                } else {
                    return null;
                }
            });
        });
        cp.putParameterModifier(Width.class, (p, builder) -> builder.setWidth(p.value()));
        cp.putParameterModifier(Contiguous.class, (p, builder) -> builder.setContiguous(p.value()));
//        cp.putParameterModifier(Numeric.class, (p, builder) -> {
//            if (!(builder instanceof CommandParameterFunctionBuilderImpl)) {
//                final TypeParser<?> parser = builder.getParser();
//                if (parser.hasPredicate()) {
//                    builder.setTypeParser(parser.getPredicate().and((arg, flags) -> arg.isNumeric()), parser.getMapper());
//                } else {
//                    builder.setTypeParser((arg, flags) -> arg.isNumeric(), parser.getMapper());
//                }
//            }
//        });
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
