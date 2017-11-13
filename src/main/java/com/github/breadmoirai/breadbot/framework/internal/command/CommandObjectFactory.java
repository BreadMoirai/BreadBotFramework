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
package com.github.breadmoirai.breadbot.framework.internal.command;

import com.github.breadmoirai.breadbot.util.ExceptionalSupplier;

import java.util.Objects;
import java.util.function.Consumer;

public class CommandObjectFactory {

    private final ExceptionalSupplier<Object> supplier;
    private Consumer<Throwable> exceptionHandler;

    public CommandObjectFactory(ExceptionalSupplier<Object> supplier) {
        this.supplier = supplier;
    }

    public void setExceptionHandler(Consumer<Throwable> exceptionHandler) {
        Objects.requireNonNull(exceptionHandler, "ExceptionHandler must not be null.");
        this.exceptionHandler = exceptionHandler;
    }

    public Object get() {
        try {
            return supplier.get();
        } catch (Throwable t) {
            exceptionHandler.accept(t);
            return null;
        }
    }

}
