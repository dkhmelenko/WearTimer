package com.cologne.hackaton.wearstopwatch.utils;

/**
 * Provides utility methods for the strings
 *
 * @author Dmytro Khmelenko
 */
public final class StringUtils {

    // denied constructor
    private StringUtils() {
    }

    /**
     * Formats the time to the suitable string
     *
     * @param time Time in milliseconds
     * @return Formatted time
     */
    public static String formatString(long time) {
        int secs = (int) (time / 1000);
        int mins = secs / 60;
        secs = secs % 60;
        int milliseconds = (int) (time % 1000);
        return "" + mins + ":" + String.format("%02d", secs) + ":"
                + String.format("%03d", milliseconds);
    }
}
