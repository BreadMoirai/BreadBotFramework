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
//import com.github.breadmoirai.bot.framework.Response;
//import com.github.breadmoirai.bot.framework.SamuraiClient;
//import com.github.breadmoirai.bot.framework.command.Command;
//import com.github.breadmoirai.bot.framework.command.CommandMethod;
//import com.github.breadmoirai.bot.framework.command.arg.CommandArgumentList;
//import com.github.breadmoirai.bot.framework.event.CommandEvent;
//
//import java.lang.invoke.MethodHandle;
//import java.lang.invoke.MethodHandles;
//import java.lang.reflect.Method;
//import java.util.function.Function;
//
//
//public class CommandMethodHandle implements CommandMethod {
//
//    private final String[] keys;
//    private final MethodHandle handle;
//    private final Class<?>[] params;
//    private final Function<CommandEvent, CommandArgumentList> argumentListFactory;
//    private final int limit;
//
//    public CommandMethodHandle(Method method) throws IllegalAccessException {
//        this.params = method.getParameterTypes();
//        handle = MethodHandles.publicLookup().unreflect(method);
//        final Command annotation = method.getAnnotation(Command.class);
//        final String[] value = annotation.value();
//        if (value.length == 0)
//        keys = new String[]{method.getName()};
//        else keys = value;
//    }
//
//    public MethodHandle getHandle() {
//        return handle;
//    }
//
//    @Override
//    public boolean handle(Object parent, CommandEvent event, int subKey) throws Throwable {
//        final SamuraiClient client = event.getClient();
//
//        if (invoke instanceof Response){
//            final Response r = (Response) invoke;
//            r.base(event);
//            r.send();
//        }
//        return true;
//    }
//
//    @Override
//    public String[] getKeys() {
//        return keys;
//    }
//}
