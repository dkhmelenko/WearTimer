package com.cologne.hackaton.wearstopwatch.activity;

import com.cologne.hackaton.wearstopwatch.timelib.model.Lap;

import java.util.Comparator;

/**
 * Compares Laps by lap number
 *
 * @author Gabriel Schlatter
 */
public class LapComparator implements Comparator<Lap> {
    @Override
    public int compare(Lap l1, Lap l2) {
        return (l1.getLapNumber() < l2.getLapNumber() ? 1 : -1);
    }
}
