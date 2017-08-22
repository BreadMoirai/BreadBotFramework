/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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
package com.github.breadmoirai.bot.framework.command;

import com.github.breadmoirai.bot.framework.event.CommandEvent;
import com.github.breadmoirai.bot.framework.IModule;
import com.github.breadmoirai.bot.framework.SamuraiClient;
import com.github.breadmoirai.bot.util.TypeFinder;

import java.lang.reflect.Type;

public abstract class BiModuleCommand<M1 extends IModule, M2 extends IModule> implements ICommand {

    private final Type[] types = TypeFinder.getTypeArguments(this.getClass(), BiModuleCommand.class);

    @Override
    public void handle(CommandEvent event) throws Throwable {
        final SamuraiClient client = event.getClient();
        //noinspection unchecked
        execute(event, (M1) client.getModule(types[0]), (M2) client.getModule(types[1]));
    }

    public abstract void execute(CommandEvent event, M1 module1, M2 module2);


}
