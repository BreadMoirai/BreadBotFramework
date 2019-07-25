/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.command.internal;

import com.github.breadmoirai.breadbot.framework.inject.BreadInjector;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class CommandObjectFactory {

    /**
     * STATIC
     **/

    private static Consumer<Throwable> exceptionHandler;

    static {
        org.slf4j.Logger logger = LoggerFactory.getLogger(CommandObjectFactory.class);
        exceptionHandler = t -> logger.error(
                "An error occurred while attempting to retrieve an instance of a command object", t);
    }

    /**
     * INSTANCE
     **/

    protected Class<?> returnType;
    protected BreadInjector.Injector injector;

    public CommandObjectFactory(Class<?> returnType) {
        this.returnType = returnType;
    }

    public static void setExceptionHandler(Consumer<Throwable> exceptionHandler) {
        Objects.requireNonNull(exceptionHandler, "ExceptionHandler must not be null.");
        CommandObjectFactory.exceptionHandler = exceptionHandler;
    }

    public static CommandObjectFactory empty() {
        return new EmptyCommandObjectFactory();
    }

    public static CommandObjectFactory of(Class<?> returnType, Object o) {
        return new SimpleCommandObjectFactory(o.getClass(), o);
    }

    public static CommandObjectFactory of(Class<?> returnType, Supplier<?> supplier) {
        return new SupplierCommandObjectFactory(returnType, supplier);
    }

    public static CommandObjectFactory of(Class<?> returnType, MethodHandle handle) {
        return new MethodHandleCommandObjectFactory(returnType, handle);
    }

    public void setInjector(BreadInjector injector) {
        if (returnType == null || injector == null) return;
        this.injector = injector.getInjectorFor(returnType);
    }

    public abstract Object get() throws Throwable;

    public final Object getOrNull() {
        try {
            return get();
        } catch (Throwable t) {
            exceptionHandler.accept(t);
            return null;
        }
    }

    private static class SimpleCommandObjectFactory extends CommandObjectFactory {

        private final Object o;

        private SimpleCommandObjectFactory(Class<?> returnType, Object o) {
            super(returnType);
            this.o = o;
        }

        @Override
        public Object get() throws IllegalAccessException {
            if (injector != null) {
                injector.inject(o);
                injector = null;
            }
            return o;
        }
    }

    private static class SupplierCommandObjectFactory extends CommandObjectFactory {

        private final Supplier<?> supplier;

        public SupplierCommandObjectFactory(Class<?> returnType, Supplier<?> supplier) {
            super(returnType);
            this.supplier = supplier;
        }

        @Override
        public Object get() throws IllegalAccessException {
            final Object o = supplier.get();
            if (injector != null) {
                injector.inject(o);
            }
            return o;
        }
    }

    private static class MethodHandleCommandObjectFactory extends CommandObjectFactory {

        private final MethodHandle handle;

        private MethodHandleCommandObjectFactory(Class<?> returnType, MethodHandle handle) {
            super(returnType);
            this.handle = handle;
        }

        @Override
        public Object get() throws Throwable {
            final Object o = handle.invoke();
            if (injector != null) {
                injector.inject(o);
            }
            return o;
        }
    }

    private static class EmptyCommandObjectFactory extends CommandObjectFactory {

        public EmptyCommandObjectFactory() {
            super(null);
        }

        @Override
        public Object get() {
            return Void.TYPE;
        }
    }
}