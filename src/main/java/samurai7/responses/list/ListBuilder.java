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
package samurai7.responses.list;

import samurai7.core.engine.CommandEvent;

import java.util.function.Function;
import java.util.function.Predicate;

public class ListBuilder<T> {

    private Function<CommandEvent, Predicate<? super T>> filter;
    private boolean select;

    /**
     * The setting of this field will allow the user to use !filter and !filterout as commands.
     * @param filter
     */
    public ListBuilder<T> onFilter(Function<CommandEvent, Predicate<? super T>> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * enables !select and !selectout
     * @param enable
     */
    public ListBuilder<T> enableSelect(boolean enable) {
        select = enable;
        return this;
    }




}
