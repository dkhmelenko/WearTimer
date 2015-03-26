package com.cologne.hackaton.wearstopwatch.timelib.utils;

/**
 * Provides time related utilities
 *
 * @author Dmytro Khmelenko
 */
public final class TimeUtils {

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;

    private static final long SECONDS_IN_MINUTE = 60;

    // denied constructor
    private TimeUtils() {
    }

    /**
     * Converts seconds to milliseconds
     *
     * @param seconds Seconds amount
     * @return Milliseconds
     */
    public static long toMilliseconds(long seconds) {
        return seconds * SECOND;
    }

    /**
     * Converts minutes and seconds to milliseconds
     *
     * @param minutes Minutes amount
     * @param seconds Seconds amount
     * @return Milliseconds
     */
    public static long toMilliseconds(long minutes, long seconds) {
        long milliseconds = minutes * MINUTE;
        milliseconds += toMilliseconds(seconds);
        return milliseconds;
    }

    /**
     * Converts hours, minutes and seconds to milliseconds
     *
     * @param hours   Hours amount
     * @param minutes Minutes amount
     * @param seconds Seconds amount
     * @return Milliseconds
     */
    public static long toMilliseconds(long hours, long minutes, long seconds) {
        long milliseconds = hours * HOUR;
        milliseconds += toMilliseconds(minutes, seconds);
        return milliseconds;
    }

    /**
     * Get the value of seconds from the total milliseconds
     *
     * @param millis Total millis
     * @return Exact seconds
     */
    public static long getSeconds(long millis) {
        long totalSeconds = millis / SECOND;
        return totalSeconds % SECONDS_IN_MINUTE;
    }

    /**
     * Gets the value of minutes from the total milliseconds
     *
     * @param millis Total millis
     * @return Exact minutes
     */
    public static long getMinutes(long millis) {
        long totalSeconds = millis / SECOND;
        return totalSeconds / SECONDS_IN_MINUTE;
    }
}
