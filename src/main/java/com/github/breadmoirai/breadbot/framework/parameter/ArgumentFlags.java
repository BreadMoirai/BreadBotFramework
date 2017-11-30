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

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds flags for ArgumentParsing.
 */
public final class ArgumentFlags {

    private static final Logger LOG = LoggerFactory.getLogger(ArgumentFlags.class);

    /**
     * Signifies that the {@link ArgumentTypeMapper} should do everything it can to map to it's type. This has no effect on numeric types.
     */
    public static final String STRICT = "strict";
    /**
     * Signifies that this numeric argument is in hexadecimal form.
     */
    public static final String HEX = "hex";

    private static final TObjectIntMap<String> CUSTOM_FLAGS;

    static {
        CUSTOM_FLAGS = new TObjectIntHashMap<>();
        registerFlag(STRICT);
        registerFlag(HEX);
    }

    public static void registerFlag(String flagName) {
        CUSTOM_FLAGS.putIfAbsent(flagName.toLowerCase(), 0b1 << CUSTOM_FLAGS.size());
    }

    public static int addFlag(int flags, String... flagNames) {
        int acc = flags;
        for (String flagName : flagNames) {
            final int flag = getFlag(flagName);
            if (flag == 0) LOG.warn("Ignoring unregistered flag: " + flagName);
            else acc = acc | flag;
        }
        return acc;
    }

    /**
     * Uses a bit-wise OR to combine the specified flag. If a flag is not found, a warning is issued.
     *
     * @param flagNames
     * @return
     */
    public static int getFlags(String... flagNames) {
        return addFlag(0, flagNames);
    }

    /**
     * Gets the bit indicator of the flag.
     * @param flagName the name of the flag
     * @return the value if found, otherwise {@code 0}
     */
    public static int getFlag(String flagName) {
        return CUSTOM_FLAGS.get(flagName.toLowerCase());
    }

    /**
     * {@code true} means that the {@link CommandArgument} should only be matched to this type when explicitly specified such as a Mention.
     * <p>If a flag is NOT strict, The mapper should try it's best to match it to the type provided such as searching for a matching name in case of a User or Channel.
     *
     * @param flags the int value.
     * @return {@code true} if the flag indicates to use a Strict Strategy.
     */
    public static boolean isStrict(int flags) {
        return hasFlag(flags, STRICT);
    }

    /**
     * checks if the provided {@code int} value contains the specified flag.
     * @param flags the flag value to test.
     * @param flagName the name of the flag.
     * @return {@code true} if the value contains the specified flag, otherwise {@code false}. If the given {@code flagName} is not registered, {@code false} will be returned.
     */
    public static boolean hasFlag(int flags, String flagName) {
        return (getFlag(flagName) & flags) != 0;
    }

    public static boolean hasFlags(int flags, String... flagNames) {
        for (String flagName : flagNames) {
            if (!hasFlag(flags, flagName)) return false;
        }
        return true;
    }

    /**
     * A private constructor for a static class.
     */
    private ArgumentFlags() {}

}
