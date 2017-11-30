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

package com.github.breadmoirai.breadbot.framework.parameter;

public class ArgumentParser<T> implements ArgumentTypeMapper<T>, ArgumentTypePredicate {

    private final ArgumentTypePredicate predicate;
    private final ArgumentTypeMapper<T> mapper;

    public ArgumentParser(ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        this.predicate = predicate;
        this.mapper = mapper;
    }

    public ArgumentParser(ArgumentTypeMapper<T> mapper) {
        this(null, mapper);
    }

    @Override
    public T map(CommandArgument arg, int flags) {
        return parse(arg, flags);
    }

    @Override
    public boolean test(CommandArgument arg, int flags) {
        return predicate != null ? predicate.test(arg, flags) : mapper.map(arg, flags) != null;
    }

    public boolean hasPredicate() {
        return predicate != null;
    }

    /**
     * Checks the argument with the predicate if the predicate is present. , returning an empty Optional if the predicate returned {@code false}. Then applies the ArgumentTypeMapper to the CommandArgument and returns the result.
     *
     * @param arg   the arg to parse
     * @param flags any flags
     * @return an optional
     */
    public T parse(CommandArgument arg, int flags) {
        if (hasPredicate()) {
            if (!predicate.test(arg, flags)) {
                return null;
            }
        }
        return mapper.map(arg, flags);
    }

    public ArgumentTypePredicate getPredicate() {
        return predicate;
    }

    public ArgumentTypeMapper<T> getMapper() {
        return mapper;
    }
}