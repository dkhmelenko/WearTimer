package com.cologne.hackaton.wearstopwatch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.cologne.hackaton.wearstopwatch.event.AttachListenersEvent;
import com.cologne.hackaton.wearstopwatch.event.CreateLapEvent;
import com.cologne.hackaton.wearstopwatch.event.LapUpdateEvent;
import com.cologne.hackaton.wearstopwatch.event.PauseStopwatchEvent;
import com.cologne.hackaton.wearstopwatch.event.RequestStatusEvent;
import com.cologne.hackaton.wearstopwatch.event.ResetStopWatchEvent;
import com.cologne.hackaton.wearstopwatch.event.SaveLapsEvent;
import com.cologne.hackaton.wearstopwatch.event.StartStopwatchEvent;
import com.cologne.hackaton.wearstopwatch.event.StatusResponseEvent;
import com.cologne.hackaton.wearstopwatch.timelib.StopWatch;
import com.cologne.hackaton.wearstopwatch.timelib.model.Lap;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Service for Stopwatch work
 *
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class StopWatchService extends Service {

    private EventBus mEventBus;

    private StopWatch mStopWatch;
    private List<Lap> mLaps;

    private StartStopwatchEvent.StopWatchTickCallback mTickCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        mLaps = new ArrayList<>();

        initStopwatch();

        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        Log.d(getClass().getSimpleName(), "StopWatchService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
        Log.d(getClass().getSimpleName(), "StopWatchService destroyed");
    }

    /**
     * Initializes stopwatch
     */
    private void initStopwatch() {
        mStopWatch = new StopWatch(new StopWatch.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(long time) {
                if(mTickCallback != null) {
                    mTickCallback.onTick(time);
                }
            }
        }, new StopWatch.OnLapsChangeListener() {
            @Override
            public void onLapAdded(Lap lap) {
                if (lap != null) {
                    mLaps.add(lap);
                    mEventBus.post(new LapUpdateEvent(mLaps));
                }
            }

            @Override
            public void onLapsCleared() {
                mEventBus.post(new SaveLapsEvent());
                mLaps.clear();
                mEventBus.post(new LapUpdateEvent(mLaps));
            }
        });
    }

    /**
     * Handles StartStopwatchEvent
     *
     * @param event Event data
     */
    public void onEvent(StartStopwatchEvent event) {
        Log.d(getClass().getSimpleName(), "StartStopwatchEvent");

        mTickCallback = event.getCallback();
        mStopWatch.startStopWatch();
    }

    /**
     * Handles PauseStopwatchEvent
     *
     * @param event Event data
     */
    public void onEvent(PauseStopwatchEvent event) {
        mStopWatch.pauseStopWatch();
        event.getCallback().receivePauseTime(mStopWatch.getCurrentTime());
    }

    /**
     * Handles ResetStopWatchEvent
     *
     * @param event Event data
     */
    public void onEvent(ResetStopWatchEvent event) {
        Log.d(getClass().getSimpleName(), "ResetStopWatchEvent");
        mEventBus.post(new LapUpdateEvent(mLaps));
        mStopWatch.resetStopWatch();
    }

    /**
     * Handles CreateLapEvent
     *
     * @param event Event data
     */
    public void onEvent(CreateLapEvent event) {
        Log.d(getClass().getSimpleName(), "CreateLapEvent");
        mStopWatch.saveLap();
    }

    /**
     * Handles AttachListenersEvent
     *
     * @param event Event data
     */
    public void onEvent(AttachListenersEvent event) {
        mTickCallback = event.getTickCallback();
        mEventBus.post(new LapUpdateEvent(mLaps));
    }

    /**
     * Handles RequestStatusEvent
     *
     * @param event Event data
     */
    public void onEvent(RequestStatusEvent event) {
        mEventBus.post(new StatusResponseEvent(mStopWatch.isRunning()));
    }
}
