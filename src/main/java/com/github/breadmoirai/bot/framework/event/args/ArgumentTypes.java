/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.bot.framework.event.args;

import com.github.breadmoirai.bot.framework.event.args.impl.ArgumentTypeImpl;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public enum ArgumentTypes {
    INTEGER, LONG, FLOAT, DOUBLE, RANGE, HEX, USER, MEMBER, ROLE, TEXTCHANNEL, EMOTE, EMOJI;

    private static final Map<Class<?>, ArgumentType<?>> map;

    private static final ArgumentType<Void> voidType;

    static {
        map = new HashMap<>();

        voidType = new ArgumentTypeImpl<>(arg -> false, arg -> null);

        final ArgumentType<Integer> intType = new ArgumentTypeImpl<>(CommandArgument::isInteger, CommandArgument::parseInt);
        map.put(Integer.TYPE, intType);
        map.put(Integer.class, intType);

        final ArgumentType<Long> longType = new ArgumentTypeImpl<>(CommandArgument::isLong, CommandArgument::parseLong);
        map.put(Long.TYPE, longType);
        map.put(Long.class, longType);

        final ArgumentType<Float> floatType = new ArgumentTypeImpl<>(CommandArgument::isFloat, CommandArgument::parseFloat);
        map.put(Float.TYPE, floatType);
        map.put(Float.class, floatType);

        final ArgumentType<Double> doubleType = new ArgumentTypeImpl<>(CommandArgument::isFloat, CommandArgument::parseDouble);
        map.put(Double.TYPE, doubleType);
        map.put(Double.class, doubleType);

        final ArgumentType<Boolean> boolType = new ArgumentTypeImpl<>(commandArgument -> {
            final String s = commandArgument.getArgument();
            return s.equals("true") || s.equals("false");
        }, commandArgument -> {
            final String s = commandArgument.getArgument();
            return s.equals("true");
        });
        map.put(Boolean.TYPE, boolType);
        map.put(Boolean.class, boolType);

        registerCustomType(IntStream.class, commandArgument -> commandArgument.isRange() || commandArgument.isInteger(), CommandArgument::parseRange);

        registerCustomType(User.class, CommandArgument::isValidUser, CommandArgument::getUser);
    }

    public static <T> void registerCustomType(Class<T> type, Predicate<CommandArgument> isType, Function<CommandArgument, T> getAsType) {
        map.put(type, new ArgumentTypeImpl<>(isType, getAsType));
    }

    public static boolean isType(Class<?> type, CommandArgument arg) {
        return map.getOrDefault(type, voidType).test(arg);
    }

    public static <T> T getAsType(Class<T> type, CommandArgument arg) {
        //noinspection unchecked
        return (T) map.getOrDefault(type, voidType).apply(arg);
    }
}
