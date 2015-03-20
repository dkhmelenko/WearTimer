package com.cologne.hackaton.wearstopwatch.model;

/**
 * Represents lap data
 *
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class Lap {
    private final long time;

    public Lap(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}