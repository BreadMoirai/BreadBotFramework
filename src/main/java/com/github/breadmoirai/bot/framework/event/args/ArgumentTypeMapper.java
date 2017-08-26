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

@FunctionalInterface
public interface ArgumentTypeMapper<T> {
    /**
     * Maps the {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument} to this given type.
     * This method is only be called after
     * {@link ArgumentTypePredicate#test(CommandArgument, int)}
     * returns {@code true}.
     *
     * @param arg The {@link com.github.breadmoirai.bot.framework.event.args.CommandArgument} to be mapped.
     * @param flags the flags. See {@link com.github.breadmoirai.bot.framework.event.args.ArgumentFlags}
     *
     * @return T
     */
    T map(CommandArgument arg, int flags);
}
