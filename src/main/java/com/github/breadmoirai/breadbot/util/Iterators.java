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

package com.github.breadmoirai.breadbot.util;

import java.util.Iterator;
import java.util.function.Function;

public class Iterators {

    /**
     * static class -{@literal >} private constructor
     */
    private Iterators() {

    }

    public static <T, R> Iterator<R> map(Iterator<T> iterator, Function<T, R> mapper) {
        return new IteratorMapper<>(iterator, mapper);
    }


    private static class IteratorMapper<T, R> implements Iterator<R> {
        private final Iterator<T> base;
        private final Function<T, R> mapper;

        public IteratorMapper(Iterator<T> base, Function<T, R> mapper) {
            this.base = base;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            return base.hasNext();
        }

        @Override
        public R next() {
            return mapper.apply(base.next());
        }

        @Override
        public void remove() {
            base.remove();
        }

    }
}