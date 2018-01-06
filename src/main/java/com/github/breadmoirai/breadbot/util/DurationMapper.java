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

import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;

import java.time.Duration;

public class DurationMapper implements TypeParser<Duration> {

    @Override
    public Duration parse(CommandArgument argument) {
        final String s = argument.getArgument().toLowerCase();
        if (s.indexOf(' ') != -1) {
            final String[] times = new String[4];
            int i = 0;
            int j = 0;
            int k = s.indexOf(':');
            while (k != -1 && i < 4) {
                String substring = s.substring(j, k);
                if (substring.length() != 0 && !Arguments.isNumber(substring)) {
                    return null;
                }
                times[i++] = substring;
                j = k + 1;
                k = s.indexOf(':', j);
            }
            switch (i) {
                case 1:
                case 2:
                    return Duration.ofSeconds((times[0].length() == 0 ? 0L
                            : Long.parseLong(times[0])) * 60
                            + ((times[1].length() == 0) ? 0L
                            : Long.parseLong(times[1])));
                case 3:
                    return Duration.ofSeconds((times[0].length() == 0 ? 0L
                            : Long.parseLong(times[0])) * 3600
                            + ((times[1].length() == 0) ? 0L
                            : Long.parseLong(times[1])) * 60
                            + ((times[2].length() == 0) ? 0L
                            : Long.parseLong(times[2])));
                case 4:
                    return Duration.ofSeconds((times[0].length() == 0 ? 0L
                            : Long.parseLong(times[0])) * 86400
                            + ((times[1].length() == 0) ? 0L
                            : Long.parseLong(times[1])) * 3600
                            + ((times[2].length() == 0) ? 0L
                            : Long.parseLong(times[2])) * 60
                            + ((times[3].length() == 0) ? 0L
                            : Long.parseLong(times[3])));
                default:
                    return null;
            }
        } else {
            final String[] split = s.split("\\s+");
            long seconds = 0;
            for (int i = 0; i < split.length - 1; i++) {
                long value;
                final String s1 = split[i];
                if (Arguments.isLong(s1)) {
                    value = Long.parseLong(s1);
                } else {
                    continue;
                }
                final String s2 = split[++i];
                switch (s2.toLowerCase()) {
                    case "s":
                    case "sec":
                    case "secs":
                    case "second":
                    case "seconds":
                        seconds += value;
                        break;
                    case "m":
                    case "min":
                    case "mins":
                    case "minute":
                    case "minutes":
                        seconds += value * 60;
                        break;
                    case "h":
                    case "hour":
                    case "hours":
                        seconds += value * 3600;
                        break;
                    case "d":
                    case "day":
                    case "days":
                        seconds += value * 86400;
                        break;
                    case "wk":
                    case "week":
                    case "weeks":
                        seconds += value * 604800;
                        break;
                    default:
                }
            }
            return Duration.ofSeconds(seconds);
        }

    }
}