package com.yeepay.g3.sdk.yop.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

public final class DateUtils {

    /**
     * Alternate ISO 8601 format without fractional seconds
     */
    private static final DateTimeFormatter ALTERNATE_ISO8601_DATE_FORMAT =
            ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

    /**
     * Formats the specified date as an ISO 8601 string.
     *
     * @param date The date to format.
     * @return The ISO 8601 string representing the specified date.
     */
    public static String formatAlternateIso8601Date(Date date) {
        return ALTERNATE_ISO8601_DATE_FORMAT.print(new DateTime(date));
    }

}
