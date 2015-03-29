package com.cologne.hackaton.wearstopwatch.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.cologne.hackaton.wearstopwatch.R;
import com.cologne.hackaton.wearstopwatch.event.RequestTimerStatusEvent;
import com.cologne.hackaton.wearstopwatch.event.ResetTimerEvent;
import com.cologne.hackaton.wearstopwatch.event.StartTimerEvent;
import com.cologne.hackaton.wearstopwatch.event.StopTimerEvent;
import com.cologne.hackaton.wearstopwatch.event.TimerAlarmEvent;
import com.cologne.hackaton.wearstopwatch.event.TimerStartedEvent;
import com.cologne.hackaton.wearstopwatch.event.TimerStatusResponseEvent;
import com.cologne.hackaton.wearstopwatch.event.TimerTickEvent;
import com.cologne.hackaton.wearstopwatch.service.StopWatchService;
import com.cologne.hackaton.wearstopwatch.timelib.utils.TimeUtils;
import com.cologne.hackaton.wearstopwatch.service.TimerService;

import de.greenrobot.event.EventBus;

/**
 * Timer activity
 *
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class TimerActivity extends Activity {

    private static final int MIN_SECONDS = 0;
    private static final int MAX_SECONDS = 59;
    private static final int MIN_MINUTES = 0;
    private static final int MAX_MINUTES = 59;

    private EventBus mEventBus;

    // views
    private Button mBtnStartStop;
    private NumberPicker mMinutesPicker;
    private NumberPicker mSecondsPicker;

    private int mMinutes;
    private int mSeconds;

    boolean mIsTimerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEventBus = EventBus.getDefault();
        mEventBus.register(this);

        setContentView(R.layout.activity_timer);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                initializeViews();

                startTimerService();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
        if (!mIsTimerRunning) {
            Intent i = new Intent(this, StopWatchService.class);
            stopService(i);
        }
    }

    /**
     * Starts timer service
     */
    private void startTimerService() {
        if (!isTimerServiceRunning(TimerService.class)) {
            Log.d(getClass().getSimpleName(), "Start Timer Service");
            Intent i = new Intent(TimerActivity.this, TimerService.class);
            startService(i);
        } else {
            // Attach to existing Service
            Log.d(getClass().getSimpleName(), "Attach Listeners");
            mEventBus.post(new RequestTimerStatusEvent());
        }
    }

    /**
     * Checks whether timer service running or not
     *
     * @param serviceClass
     * @return True, if service is running. False otherwise
     */
    private boolean isTimerServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes views
     */
    private void initializeViews() {
        mMinutesPicker = (NumberPicker) findViewById(R.id.np_minutes);
        mMinutesPicker.setMinValue(MIN_MINUTES);
        mMinutesPicker.setMaxValue(MAX_MINUTES);
        mMinutesPicker.setWrapSelectorWheel(false);

        mSecondsPicker = (NumberPicker) findViewById(R.id.np_seconds);
        mSecondsPicker.setMinValue(MIN_SECONDS);
        mSecondsPicker.setMaxValue(MAX_SECONDS);
        mSecondsPicker.setWrapSelectorWheel(false);

        mBtnStartStop = (Button) findViewById(R.id.btn_start);
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsTimerRunning) {
                    mSeconds = mSecondsPicker.getValue();
                    mMinutes = mMinutesPicker.getValue();
                    if (mMinutes > 0 || mSeconds > 0) {
                        mEventBus.post(new StartTimerEvent(mSeconds, mMinutes));
                    }
                } else {
                    mEventBus.post(new StopTimerEvent());
                }
            }
        });
    }

    /**
     * Handles event TimerTickEvent
     *
     * @param event Event data
     */
    public void onEvent(TimerTickEvent event) {
        int minutes = (int) TimeUtils.getMinutes(event.getMillisUntil());
        int secs = (int) TimeUtils.getSeconds(event.getMillisUntil());
        Log.d(getClass().getSimpleName(), "Tick: " + minutes + " : " + secs);

        mMinutesPicker.setMinValue(minutes);
        mMinutesPicker.setMaxValue(minutes);
        mSecondsPicker.setMinValue(secs);
        mSecondsPicker.setMaxValue(secs);
    }

    /**
     * Handles event TimerStartedEvent
     *
     * @param event Event data
     */
    public void onEvent(TimerStartedEvent event) {
        onStartTimer();
    }

    /**
     * Handles event ResetTimerEvent
     *
     * @param event Event data
     */
    public void onEvent(ResetTimerEvent event) {
        onResetTimer();
    }

    /**
     * Handles event TimerStatusResponseEvent
     *
     * @param event Event data
     */
    public void onEvent(TimerStatusResponseEvent event) {
        if (event.isRunning()) {
            onStartTimer();
        } else {
            mIsTimerRunning = false;
        }
    }

    /**
     * Handles event TimerAlarmEvent
     *
     * @param event Event data
     */
    public void onEvent(TimerAlarmEvent event) {
        Toast.makeText(this, R.string.timer_finished, Toast.LENGTH_SHORT).show();

        onResetTimer();
    }

    /**
     * Called on start timer
     */
    private void onStartTimer() {
        mMinutesPicker.setEnabled(false);
        mSecondsPicker.setEnabled(false);

        mBtnStartStop.setText(R.string.timer_stop);
        mIsTimerRunning = true;
    }

    /**
     * Called on reset timer data
     */
    private void onResetTimer() {
        resetMinutesPicker();
        resetSecondsPicker();

        mSecondsPicker.setValue(mSeconds);
        mMinutesPicker.setValue(mMinutes);

        mBtnStartStop.setText(R.string.timer_start);
        mIsTimerRunning = false;
    }

    /**
     * Resets minutes picker to default
     */
    private void resetMinutesPicker() {
        mMinutesPicker.setMinValue(MIN_MINUTES);
        mMinutesPicker.setMaxValue(MAX_MINUTES);
        mMinutesPicker.setEnabled(true);
    }

    /**
     * Resets seconds picket to default
     */
    private void resetSecondsPicker() {
        mSecondsPicker.setMinValue(MIN_SECONDS);
        mSecondsPicker.setMaxValue(MAX_SECONDS);
        mSecondsPicker.setEnabled(true);
    }
}
