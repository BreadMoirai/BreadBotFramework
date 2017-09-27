///*
// *       Copyright 2017 Ton Ly (BreadMoirai)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//package com.github.breadmoirai.bot.framework.command;
//
//import com.github.breadmoirai.bot.framework.IModule;
//import com.github.breadmoirai.bot.framework.event.CommandEvent;
//import com.github.breadmoirai.bot.util.TypeFinder;
//
//import java.lang.reflect.Type;
//
///**
// * Commands should be derived from either this or {@link BiModuleCommand}.
// *
// * <p><b>See example:</b>
// * <pre><code>
// * public class NowPlaying extends {@link ModuleCommand Command}{@literal <MusicModule>} {
// *   {@literal @}Override
// *    public void handle(CommandEvent event, MusicModule module) {
// *        event.reply(module.getNowPlaying());
// *    }
// * }</code></pre>
// *
// * @param <M> The Module of the command
// */
//public abstract class ModuleCommand<M extends IModule> implements ICommand {
//
//    private Type moduleType = TypeFinder.getTypeArguments(this.getClass(), ModuleCommand.class)[0];
//
//    @Override
//    public final void handle(CommandEvent event) {
//        @SuppressWarnings("unchecked")
//        M module = (M) event.getClient().getModule(moduleType);
//        handle(event, module);
//    }
//
//    public abstract void handle(CommandEvent event, M module);
//
//    @Override
//    public String toString() {
//        return String.format("%s<%s>", this.getClass().getSimpleName(), moduleType.getTypeName());
//    }
//}
