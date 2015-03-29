package com.cologne.hackaton.wearstopwatch.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.cologne.hackaton.wearstopwatch.R;
import com.cologne.hackaton.wearstopwatch.event.RequestTimerStatusEvent;
import com.cologne.hackaton.wearstopwatch.event.ResetTimerEvent;
import com.cologne.hackaton.wearstopwatch.event.StartTimerEvent;
import com.cologne.hackaton.wearstopwatch.event.StopTimerEvent;
import com.cologne.hackaton.wearstopwatch.event.TimerAlarmEvent;
import com.cologne.hackaton.wearstopwatch.event.TimerStartedEvent;
import com.cologne.hackaton.wearstopwatch.event.TimerStatusResponseEvent;
import com.cologne.hackaton.wearstopwatch.event.TimerTickEvent;
import com.cologne.hackaton.wearstopwatch.timelib.utils.TimeUtils;

import de.greenrobot.event.EventBus;

/**
 * Timer activity
 *
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class TimerService extends Service {

    private static final int NOTIFICATION_ID = 0;

    private EventBus mEventBus;

    private CountDownTimer mTimer;

    private boolean mIsRunning;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
    }

    /**
     * Handles event StartTimerEvent
     *
     * @param event Event data
     */
    public void onEvent(StartTimerEvent event) {
        Log.d(getClass().getSimpleName(), "Start Timer");
        long milliseconds = TimeUtils.toMilliseconds(event.getMinutes(), event.getSeconds());
        start(milliseconds);
    }

    /**
     * Handles event StopTimerEvent
     *
     * @param event Event data
     */
    public void onEvent(StopTimerEvent event) {
        Log.d(getClass().getSimpleName(), "Stop Timer");
        stop();
    }

    /**
     * Handles event RequestTimerStatusEvent
     *
     * @param event Event data
     */
    public void onEvent(RequestTimerStatusEvent event) {
        mEventBus.post(new TimerStatusResponseEvent(mIsRunning));
    }

    /**
     * Starts timer action
     *
     * @param timeSpan Time span
     */
    private void start(long timeSpan) {
        mTimer = new CountDownTimer(timeSpan, TimeUtils.SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                mEventBus.post(new TimerTickEvent(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                reset();
                showNotification();
            }
        };
        mTimer.start();
        mEventBus.post(new TimerStartedEvent());
        mIsRunning = true;
    }

    /**
     * Stops timer
     */
    private void stop() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        reset();
    }

    /**
     * Resets timer
     */
    private void reset() {
        mEventBus.post(new ResetTimerEvent());
    }

    /**
     * Shows notification about elapsed timer
     */
    private void showNotification() {
        NotificationManager notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = buildNotification();
        notificationManger.notify(NOTIFICATION_ID, notification);

        mIsRunning = false;
        mEventBus.post(new TimerAlarmEvent());
    }

    /**
     * Builds notification object
     *
     * @return Notification
     */
    private Notification buildNotification() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.timer_finished))
                .setSmallIcon(R.drawable.sandclock_gray)
                .setVibrate(new long[]{0, 500, 500, 500})
                .extend(new Notification.WearableExtender()
                        .setHintHideIcon(true)
                        .setContentIcon(R.drawable.sandclock_gray));

        return builder.build();
    }

}
