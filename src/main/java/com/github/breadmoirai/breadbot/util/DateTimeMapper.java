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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeMapper implements Function<CommandArgument, OffsetDateTime> {

    DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient()
            .appendPattern("[[MMMM][MMM][' ']d'th'[' ']][M/d[' ']]")
            .appendPattern("[h[':'mm[':'ss]][' ']a[' ']]")
            .appendPattern("[z][0][x]")
            .toFormatter();
    Pattern DAY_SUFFIX = Pattern.compile("(?<=[0-9])(st|nd|rd|th)");
    Pattern MONTH_DAY = Pattern.compile("[a-zA-Z]+ ([0-9]+?!(st|nd|rd|th))");


    @Override
    public OffsetDateTime apply(CommandArgument argument) {
        final OffsetDateTime base = argument.getEvent().getTime();
        String args = argument.getArgument().toLowerCase();
        final Matcher monthDay = MONTH_DAY.matcher(args);
        if (monthDay.find()) {
            if (monthDay.start() == 0) {
                args = args.substring(0, monthDay.end() + 1) + "th" + args.substring(monthDay.end() + 1);
            }
        } else
            args = DAY_SUFFIX.matcher(args).replaceAll("th");
        TemporalAccessor time;
        try {
            time = DATE_TIME_FORMATTER.parse(args);
        } catch (DateTimeParseException e) {
            return null;
        }

        boolean hasDate = false;
        final LocalTime localTime;
        if (time.isSupported(ChronoField.NANO_OF_DAY)) {
            localTime = LocalTime.from(time);
        } else {
            localTime = LocalTime.MIDNIGHT;
        }
        LocalDate localDate;
        if (time.isSupported(ChronoField.MONTH_OF_YEAR) && time.isSupported(ChronoField.DAY_OF_MONTH)) {
            localDate = LocalDate.of(base.getYear(), Month.from(time), time.get(ChronoField.DAY_OF_MONTH));
            hasDate = true;
        } else if (time.isSupported(ChronoField.DAY_OF_MONTH)) {
            localDate = LocalDate.of(base.getYear(), base.getMonth(), time.get(ChronoField.DAY_OF_MONTH));
            if (localDate.isBefore(base.toLocalDate()) || (localDate.isEqual(base.toLocalDate()) && localTime.isBefore(base.toLocalTime()))) {
                localDate = localDate.withMonth(localDate.getMonth().plus(1).getValue());
            }
            hasDate = true;
        } else {
            localDate = base.toLocalDate();
        }
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        OffsetDateTime offsetDateTime;
        if (time.isSupported(ChronoField.OFFSET_SECONDS)) {
            offsetDateTime = localDateTime.atOffset(ZoneOffset.from(time));
        } else {
            try {
                offsetDateTime = localDateTime.atZone(ZoneId.from(time)).toOffsetDateTime();
            } catch (DateTimeException e) {
                return OffsetDateTime.MIN;
            }
        }
        if (offsetDateTime.isBefore(base)) {
            if (hasDate)
                offsetDateTime = offsetDateTime.plusYears(1);
            else offsetDateTime = offsetDateTime.plusDays(1);
        } else if (!hasDate && offsetDateTime.minusDays(1).isAfter(base)) {
            offsetDateTime = offsetDateTime.minusDays(1);
        }
        return offsetDateTime;
    }
}