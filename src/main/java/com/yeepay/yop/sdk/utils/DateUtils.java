package com.yeepay.yop.sdk.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;
import java.util.Locale;

/**
 * Utilities for parsing and formatting dates.
 * <p>
 * <p>
 * Note that this class doesn't use static methods because of the synchronization issues with SimpleDateFormat. This
 * lets synchronization be done on a per-object level, instead of on a per-class level.
 */
public class DateUtils {

    /**
     * ISO 8601 format
     */
    private static final DateTimeFormatter iso8601DateFormat = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    /**
     * Alternate ISO 8601 format without fractional seconds
     */
    private static final DateTimeFormatter alternateIso8601DateFormat =
            ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

    /**
     * RFC 822 format
     */
    private static final DateTimeFormatter rfc822DateFormat =
            DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withLocale(Locale.US).withZone(DateTimeZone.UTC);

    /**
     * This is another ISO 8601 format that's used in clock skew error response
     */
    private static final DateTimeFormatter compressedIso8601DateTimeFormat =
            ISODateTimeFormat.basicDateTimeNoMillis().withZone(DateTimeZone.UTC);


    private static final DateTimeFormatter compressedIso8601DateFormat =
            ISODateTimeFormat.basicDate().withZone(DateTimeZone.UTC);

    private static final DateTimeFormatter simpleDateFromat = ISODateTimeFormat.date();

    private static final DateTimeFormatter simpleDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Parses the specified date string as an ISO 8601 date and returns the Date object.
     *
     * @param dateString The date string to parse.
     * @return The parsed Date object.
     * @throws IllegalArgumentException If the date string could not be parsed.
     */
    public static Date parseIso8601Date(String dateString) {
        try {
            return DateUtils.iso8601DateFormat.parseDateTime(dateString).toDate();
        } catch (IllegalArgumentException e) {
            // If the first ISO 8601 parser didn't work, try the alternate
            // version which doesn't include fractional seconds
            return DateUtils.alternateIso8601DateFormat.parseDateTime(dateString).toDate();
        }
    }

    /**
     * Formats the specified date as an ISO 8601 string.
     *
     * @param date The date to format.
     * @return The ISO 8601 string representing the specified date.
     */
    public static String formatIso8601Date(Date date) {
        return DateUtils.iso8601DateFormat.print(new DateTime(date));
    }

    /**
     * Parses the specified date string as an ISO 8601 date and returns the Date object.
     *
     * @param dateString The date string to parse.
     * @return The parsed Date object.
     * @throws IllegalArgumentException If the date string could not be parsed.
     */
    public static Date parseAlternateIso8601Date(String dateString) {
        return DateUtils.alternateIso8601DateFormat.parseDateTime(dateString).toDate();
    }

    /**
     * Formats the specified date as an ISO 8601 string.
     *
     * @param date The date to format.
     * @return The ISO 8601 string representing the specified date.
     */
    public static String formatAlternateIso8601Date(Date date) {
        return DateUtils.alternateIso8601DateFormat.print(new DateTime(date));
    }

    /**
     * Parses the specified date string as an RFC 822 date and returns the Date object.
     *
     * @param dateString The date string to parse.
     * @return The parsed Date object.
     * @throws IllegalArgumentException If the date string could not be parsed.
     */
    public static Date parseRfc822Date(String dateString) {
        return DateUtils.rfc822DateFormat.parseDateTime(dateString).toDate();
    }

    /**
     * Formats the specified date as an RFC 822 string.
     *
     * @param date The date to format.
     * @return The RFC 822 string representing the specified date.
     */
    public static String formatRfc822Date(Date date) {
        return DateUtils.rfc822DateFormat.print(new DateTime(date));
    }

    /**
     * Parses the specified date string as a compressedIso8601DateTimeFormat ("yyyyMMdd'T'HHmmss'Z'") and returns the Date
     * object.
     *
     * @param dateString The date string to parse.
     * @return The parsed Date object.
     * @throws IllegalArgumentException If the date string could not be parsed.
     */
    public static Date parseCompressedIso8601Date(String dateString) {
        return DateUtils.compressedIso8601DateTimeFormat.parseDateTime(dateString).toDate();
    }

    /**
     * Returns a string representation of the given date time in yyyyMMdd
     * format. The date returned is in the UTC zone.
     * <p>
     * For example, given a time "1416863450581", this method returns "20141124"
     */
    public static String formatCompressedIso8601Date(Date date) {
        return compressedIso8601DateFormat.print(new DateTime(date));
    }

    /**
     * Returns a string representation of the given date time in yyyyMMdd
     * format. The date returned is in the UTC zone.
     * <p>
     * For example, given a time "1416863450581", this method returns "20141124"
     */
    public static String formatCompressedIso8601Date(LocalDate date) {
        return compressedIso8601DateFormat.print(date);
    }


    /**
     * Returns a string representation of the given date time in
     * yyyyMMdd'T'HHmmss'Z' format. The date returned is in the UTC zone.
     * <p>
     * For example, given a time "1416863450581", this method returns
     * "20141124T211050Z"
     */
    public static String formatCompressedIso8601DateTime(Date date) {
        return compressedIso8601DateTimeFormat.print(new DateTime(date));
    }

    /**
     * Returns a string representation of the given date time in
     * yyyyMMdd'T'HHmmss'Z' format. The date returned is in the UTC zone.
     * <p>
     * For example, given a time "1416863450581", this method returns
     * "20141124T211050Z"
     */
    public static String formatCompressedIso8601DateTime(DateTime dateTime) {
        return compressedIso8601DateTimeFormat.print(dateTime);
    }

    public static String formatSimpleDateTime(DateTime dateTime) {
        return simpleDateTimeFormat.print(dateTime);
    }

    public static DateTime parseSimpleDateTime(String dateTimeString) {
        return simpleDateTimeFormat.parseDateTime(dateTimeString);
    }

    public static String formatSimpleDate(LocalDate date) {
        return simpleDateFromat.print(date);
    }

    public static DateTime parseSimpleDate(String dateString) {
        return simpleDateFromat.parseDateTime(dateString);
    }
}
