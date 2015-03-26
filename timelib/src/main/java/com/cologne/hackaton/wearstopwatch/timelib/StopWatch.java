package com.cologne.hackaton.wearstopwatch.timelib;

import android.os.Handler;

import com.cologne.hackaton.wearstopwatch.timelib.model.Lap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class StopWatch {

    // Async
    private Handler mHandler;
    private Runnable mUpdaterTask;

    // Time
    private long mLastLapTime = 0L;
    private long mStartTime = 0L;
    private long mTimeInMilliseconds = 0L;
    private long mTimeSwapBuffer = 0L;
    private long mUpdatedTime = 0L;

    // Laps
    private List<Lap> mLaps = new ArrayList<>();

    // Callbacks
    private OnTimeChangedListener mOnTimeChangedListener;
    private OnLapsChangeListener mOnLapsChangeListener;

    // Status
    private boolean mRunning;

    public StopWatch(OnTimeChangedListener onTimeChangedListener,
                     OnLapsChangeListener onLapsChangeListener) {

        mOnTimeChangedListener = onTimeChangedListener;
        mOnLapsChangeListener = onLapsChangeListener;

        mHandler = new Handler();
        mUpdaterTask = new Runnable() {
            @Override
            public void run() {
                mTimeInMilliseconds = System.currentTimeMillis() - mStartTime;
                mUpdatedTime = mLastLapTime + mTimeSwapBuffer + mTimeInMilliseconds;
                mOnTimeChangedListener.onTimeChanged(mUpdatedTime);
                mHandler.postDelayed(this, 0);
            }
        };
    }

    /**
     * Launches stop watch timer
     */
    public void startStopWatch() {
        mStartTime = System.currentTimeMillis();
        mHandler.post(mUpdaterTask);
        mRunning = true;
    }

    /**
     * Pauses stopwatch timer
     */
    public void pauseStopWatch() {
        mTimeSwapBuffer += mTimeInMilliseconds;
        mHandler.removeCallbacks(mUpdaterTask);
        mRunning = false;
    }

    /**
     * Does saving the lap time
     */
    public void saveLap() {

        long lapTime = mUpdatedTime - mLastLapTime;
        Lap newLap = new Lap(mLaps.size() + 1, lapTime, mUpdatedTime);
        mLaps.add(newLap);
        mLastLapTime = mUpdatedTime;
        mOnLapsChangeListener.onLapAdded(newLap);
        mStartTime = System.currentTimeMillis();
        mTimeInMilliseconds = 0L;
        mTimeSwapBuffer = 0L;
        mUpdatedTime = 0L;
    }

    /**
     * Resets stop watch timer
     */
    public void resetStopWatch() {
        mLastLapTime = 0L;
        mStartTime = 0L;
        mTimeInMilliseconds = 0L;
        mTimeSwapBuffer = 0L;
        mUpdatedTime = 0L;
        mLaps.clear();
        mOnLapsChangeListener.onLapsCleared();
        mOnTimeChangedListener.onTimeChanged(mUpdatedTime);
    }

    /**
     * Gets current stopwatch time
     *
     * @return Stopwatch time
     */
    public long getCurrentTime() {
        return mUpdatedTime;
    }

    /**
     * Defines whether the stopwatch running or not
     *
     * @return True, if stopwatch is running. False otherwise
     */
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Listener for handling time changes
     */
    public interface OnTimeChangedListener {

        /**
         * Called when stopwatch time changed
         *
         * @param time Changed time
         */
        public void onTimeChanged(long time);
    }

    /**
     * Listener for handling lap changes
     */
    public interface OnLapsChangeListener {

        /**
         * Called when the lap was added
         *
         * @param lap Added lap
         */
        public void onLapAdded(Lap lap);

        /**
         * Called when the laps were cleared
         */
        public void onLapsCleared();
    }

}
