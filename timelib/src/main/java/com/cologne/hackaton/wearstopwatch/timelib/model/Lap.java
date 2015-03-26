package com.cologne.hackaton.wearstopwatch.timelib.model;

import java.io.Serializable;

/**
 * Represents lap data
 *
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class Lap implements Serializable {

    private final int mLapNumber;
    private final long mLapTime;
    private final long mTimeSum;

    public Lap(int lapNumber, long lapTime, long timeSum) {
        this.mLapNumber = lapNumber;
        this.mLapTime = lapTime;
        this.mTimeSum = timeSum;
    }

    public long getLapTime() {
        return mLapTime;
    }

    public long getTimeSum() {
        return mTimeSum;
    }

    public int getLapNumber() {
        return mLapNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Lap that = (Lap) obj;
        return mLapNumber == that.mLapNumber
                && mLapTime == that.mLapTime
                && mTimeSum == that.mTimeSum;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.valueOf(mLapNumber).hashCode();
        result = prime * result + Long.valueOf(mLapTime).hashCode();
        result = prime * result + Long.valueOf(mTimeSum).hashCode();
        return result;
    }


}