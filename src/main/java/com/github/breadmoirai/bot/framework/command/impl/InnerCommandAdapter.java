///*    Copyright 2017 Ton Ly
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//*/
//package com.github.breadmoirai.bot.framework.command.impl;
//
//import com.github.breadmoirai.bot.framework.command.Command;
//import com.github.breadmoirai.bot.framework.command.CommandMethod;
//import com.github.breadmoirai.bot.framework.event.CommandEvent;
//
//import java.lang.invoke.MethodHandle;
//import java.lang.invoke.MethodHandles;
//import java.lang.invoke.MethodType;
//
//public class InnerCommandAdapter implements CommandMethod {
//
//    private final String[] keys;
//    private final HandleMap handles;
//    private final MethodHandle constructor;
//
//    public InnerCommandAdapter(Class<?> commandClass) throws NoSuchMethodException, IllegalAccessException {
//        constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class, commandClass.getEnclosingClass()));
//        handles = new HandleMap(commandClass);
//
//
//        final Command key = commandClass.getAnnotation(Command.class);
//        final String[] keyValues = key.value();
//        if (keyValues.length == 0) {
//            String name = commandClass.getSimpleName().toLowerCase();
//            if (!name.startsWith("command") && name.endsWith("command")) {
//                name = name.replace("command", "");
//            }
//            keys = new String[]{name};
//        } else {
//            keys = keyValues;
//        }
//    }
//
//    @Override
//    public boolean handle(Object parent, CommandEvent event, int subKey) throws Throwable {
//        final Object commandObj = constructor.invoke(parent);
//        if (event.getArgumentCount() > subKey) {
//            final CommandMethod commandHandle = handles.get(event.getArgumentAt(subKey).getArgument());
//            if (commandHandle != null)
//                return commandHandle.handle(commandObj, event, subKey + 1);
//        }
//        final CommandMethod defaultHandle = handles.get("");
//        if (defaultHandle != null) return defaultHandle.handle(commandObj, event, subKey + 1);
//        return false;
//}
//
//    @Override
//    public String[] getKeys() {
//        return keys;
//    }
//}
