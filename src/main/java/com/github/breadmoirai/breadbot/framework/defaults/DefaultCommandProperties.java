/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Author;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Contiguous;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Hexadecimal;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Index;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.MatchRegex;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Numeric;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Required;
import com.github.breadmoirai.breadbot.framework.annotation.parameter.Width;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertiesManagerImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.builder.CommandHandleBuilderInternal;
import com.github.breadmoirai.breadbot.framework.error.BreadBotException;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgumentList;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.ArgumentParserImpl;
import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CommandParameterBuilderImpl;
import com.github.breadmoirai.breadbot.util.Arguments;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DefaultCommandProperties {

    private static final Pattern COMMAND_PATTERN = Pattern.compile(".+(command|cmd)(s)?");

    public void initialize(CommandPropertiesManagerImpl cp) {
        putCommandModifiers(cp);

        putParameterModifiers(cp);

    }

    private void putParameterModifiers(CommandPropertiesManagerImpl cp) {
        cp.putParameterModifier(Name.class, (p, builder) -> builder.setName(p.value()));
        cp.putParameterModifier(AbsentArgumentHandler.class, (p, builder) -> builder.setOnAbsentArgument(p));
        cp.putParameterModifier(Required.class, (p, builder) -> builder.setRequired(true));
        cp.putParameterModifier(Index.class, (p, builder) -> builder.setIndex(p.value()));
        cp.putParameterModifier(MatchRegex.class, (p, builder) -> {
            final String value = p.value();
            final Pattern compile;
            try {
                //noinspection MagicConstant
                compile = Pattern.compile(value, p.flags());
            } catch (PatternSyntaxException e) {
                throw new BreadBotException("An invalid pattern was provided at " + builder.getDeclaringParameter());
            }
            builder.addArgumentPredicate(arg -> arg.matches(compile));
        });
        cp.putParameterModifier(Width.class, (p, builder) -> builder.setWidth(p.value()));
        cp.putParameterModifier(Contiguous.class, (p, builder) -> builder.setContiguous(p.value()));
        cp.putParameterModifier(Hexadecimal.class, (p, builder) -> {
            final Class<?> type = builder.getDeclaringParameter().getType();
            if (type == Integer.class || type == int.class) {
                builder.setTypeParser(arg -> arg.isHex() ? arg.parseIntFromHex() : null);
            } else if (type == Long.class || type == long.class) {
                builder.setTypeParser(arg -> arg.isHex() ? Long.parseLong(Arguments.stripHexPrefix(arg.getArgument()), 16) : null);
            } else if (type == String.class || type == CommandArgument.class) {
                builder.addArgumentPredicate(CommandArgument::isHex);
            }
        });
        cp.putParameterModifier(Numeric.class, (p, builder) -> {
            switch (p.value()) {
                case NUMBER:
                    builder.addArgumentPredicate(CommandArgument::isNumeric);
                    break;
                case INT:
                    builder.addArgumentPredicate(CommandArgument::isInteger);
                    break;
                case LONG:
                    builder.addArgumentPredicate(CommandArgument::isLong);
                    break;
                case FLOAT:
                    builder.addArgumentPredicate(CommandArgument::isFloat);
                    break;
            }
        });
        cp.putParameterModifier(Author.class, (prop, param) -> {
            final Class<?> type = param.getDeclaringParameter().getType();
            if (type == Member.class) {
                if (!prop.unlessMention()) {
                    param.setTypeParser(null);
                    param.setParser((parameter, list, parser) -> parser.getEvent().getMember());
                } else {
                    ((CommandParameterBuilderImpl) param).setArgumentParser(p -> new ArgumentParser() {
                        private final ArgumentParserImpl argumentParser = new ArgumentParserImpl(p.getIndex(), p.getWidth(), false, null, p.getTypeParser());

                        @Override
                        public Object parse(CommandParameter parameter, CommandArgumentList list, CommandParser parser) {
                            final Object parse = argumentParser.parse(parameter, list, parser);
                            if (parse != null) {
                                return parse;
                            } else {
                                return parser.getEvent().getMember();
                            }
                        }
                    });
                }
            } else if (type == User.class) {
                if (!prop.unlessMention()) {
                    param.setTypeParser(null);
                    param.setParser((parameter, list, parser) -> parser.getEvent().getAuthor());
                } else {
                    ((CommandParameterBuilderImpl) param).setArgumentParser(p -> new ArgumentParser() {
                        private final ArgumentParserImpl argumentParser = new ArgumentParserImpl(p.getIndex(), p.getWidth(), false, null, p.getTypeParser());

                        @Override
                        public Object parse(CommandParameter parameter, CommandArgumentList list, CommandParser parser) {
                            final Object parse = argumentParser.parse(parameter, list, parser);
                            if (parse != null) {
                                return parse;
                            } else {
                                return parser.getEvent().getAuthor();
                            }
                        }
                    });
                }
            }
        });
    }

    private void putCommandModifiers(CommandPropertiesManagerImpl cp) {
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
