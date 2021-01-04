/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.annotation.parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
public @interface Numeric {

    Type value() default Type.NUMBER;

    public enum Type {
        /**
         * Tests whether this argument consists of only 0-9.
         */
        NUMBER,
        /**
         * Tests whether this argument only consists of 0-9
         * and if it falls into the range of a java integer.
         */
        INT,
        /**
         * Tests whether this argument only consists of 0-9
         * and if it falls into the range of a java long.
         */
        LONG,
        /**
         * Tests whether this argument can be parsed as a float.
         *
         * @see Double#valueOf(String)
         */
        FLOAT
    }

}