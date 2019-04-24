package com.yeepay.g3.sdk.yop.utils;

import com.yeepay.g3.sdk.yop.utils.time.CachingDateFormatter;

public final class DateUtils {

    private DateUtils() {
        // do nothing
    }

    /**
     * This is another ISO 8601 format that's used in clock skew error response
     */
    private static final CachingDateFormatter iso8601DateFormat = new CachingDateFormatter("yyyyMMdd'T'HHmmss'Z'");

    /**
     * Returns a string representation of the given date time in
     * yyyyMMdd'T'HHmmss'Z' format. The date returned is in the UTC zone.
     * <p>
     * For example, given a time "1416863450581", this method returns
     * "20141124T211050Z"
     */
    public static String formatCompressedIso8601Timestamp(long timeMilli) {
        return iso8601DateFormat.format(timeMilli);
    }

}
