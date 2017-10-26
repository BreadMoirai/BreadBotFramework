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
package com.github.breadmoirai.breadbot.framework.command.parameter;

import java.util.function.Predicate;

@FunctionalInterface
public interface ArgumentTypePredicate extends Predicate<CommandArgument> {

    boolean test(CommandArgument arg, int flags);

    default boolean test(CommandArgument arg) {
        return test(arg, 0);
    }

    default ArgumentTypePredicate and(ArgumentTypePredicate other) {
        return (arg, flags) -> test(arg, flags) && other.test(arg, flags);
    }

    default ArgumentTypePredicate or(ArgumentTypePredicate other) {
        return (arg, flags) -> test(arg, flags) || other.test(arg, flags);
    }
}