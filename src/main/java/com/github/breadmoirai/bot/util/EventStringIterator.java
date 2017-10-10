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
package com.github.breadmoirai.bot.util;

import com.github.breadmoirai.bot.framework.command.parameter.CommandArgument;
import com.github.breadmoirai.bot.framework.event.CommandEvent;

import java.util.Iterator;

public class EventStringIterator implements Iterator<String> {
    private final Iterator<CommandArgument> base;

    public EventStringIterator(CommandEvent event) {
        this.base = event.getArguments().iterator();
    }

    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    @Override
    public String next() {
        return base.next().getArgument();
    }

    @Override
    public void remove() {
        base.remove();
    }

}