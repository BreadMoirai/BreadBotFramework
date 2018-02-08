package com.github.breadmoirai.breadbot.util;

import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DurationUnitMapper implements TypeParser<Duration> {

    @Override
    public Duration parse(CommandArgument arg) {
        final String argument = arg.getArgument();
        final char[] chars = argument.toCharArray();
        int i = 0;
        int time = 0;
        while (Character.isDigit(chars[i++])) {
            time *= 10;
            time += Character.getNumericValue(chars[i]);
        }
        if (time <= 0) {
            return null;
        }
        //noinspection StatementWithEmptyBody
        while (Character.isWhitespace(chars[i++])) ;

        final String unitS = argument.substring(i).toLowerCase();
        final ChronoUnit unit;

        switch (unitS) {
            case "s":
            case "sec":
            case "secs":
            case "second":
            case "seconds":
                unit = ChronoUnit.SECONDS;
                break;
            case "m":
            case "min":
            case "mins":
            case "minute":
            case "minutes":
                unit = ChronoUnit.MINUTES;
                break;
            case "h":
            case "hr":
            case "hrs":
            case "hour":
            case "hours":
                unit = ChronoUnit.HOURS;
                break;
            case "d":
            case "day":
            case "days":
                unit = ChronoUnit.DAYS;
                break;
            case "wk":
            case "wks":
            case "week":
            case "weeks":
                unit = ChronoUnit.WEEKS;
                break;
            default:
                return null;
        }

        return Duration.of(time, unit);
    }
}
