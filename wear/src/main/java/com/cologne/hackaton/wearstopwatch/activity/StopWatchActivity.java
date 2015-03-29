package com.cologne.hackaton.wearstopwatch.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cologne.hackaton.wearstopwatch.R;
import com.cologne.hackaton.wearstopwatch.event.AttachListenersEvent;
import com.cologne.hackaton.wearstopwatch.event.CreateLapEvent;
import com.cologne.hackaton.wearstopwatch.event.LapUpdateEvent;
import com.cologne.hackaton.wearstopwatch.event.PauseStopwatchEvent;
import com.cologne.hackaton.wearstopwatch.event.RequestStatusEvent;
import com.cologne.hackaton.wearstopwatch.event.ResetStopWatchEvent;
import com.cologne.hackaton.wearstopwatch.event.SaveLapsEvent;
import com.cologne.hackaton.wearstopwatch.event.StartStopwatchEvent;
import com.cologne.hackaton.wearstopwatch.event.StatusResponseEvent;
import com.cologne.hackaton.wearstopwatch.service.StopWatchService;
import com.cologne.hackaton.wearstopwatch.timelib.model.Lap;
import com.cologne.hackaton.wearstopwatch.timelib.utils.SerializeUtils;
import com.cologne.hackaton.wearstopwatch.timelib.utils.StringUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Activity for Stopwatch functionality
 *
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class StopWatchActivity extends Activity implements
        DataApi.DataListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private static final String START_STOPWATCH_PATH = "start/stopwatch";
    private static final String NEW_LAP_MESSAGE = "save/laptime";
    private static final String RESET_STOPWATCH = "reset/watch";

    private EventBus eventBus = EventBus.getDefault();

    // Views
    private TextView mTimeView;
    private ImageButton mBtnStart;
    private ImageButton mBtnStop;
    private ImageButton mBtnLock;

    // Statuses
    private boolean mLocked;
    private boolean mIsRunning;

    // Laps
    private List<Lap> mLaps = new ArrayList<>();
    private LapAdapter mLapsAdapter;
    private ListView mLapsListView;

    // Wear Api
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .build();

        eventBus.register(this);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                initializeViews();

                startStopwatchService();
            }
        });
    }

    /**
     * Launches stopwatch service
     */
    private void startStopwatchService() {
        if (!isStopwatchServiceRunning(StopWatchService.class)) {
            Intent i = new Intent(StopWatchActivity.this, StopWatchService.class);
            startService(i);
        } else {
            // Attach to existing Service
            Log.d(getClass().getSimpleName(), "Attach Listeners");
            eventBus.post(new AttachListenersEvent(
                    new StartStopwatchEvent.StopWatchTickCallback() {
                        @Override
                        public void onTick(long elapsedTime) {
                            String formattedTime = StringUtils.formatString(elapsedTime);
                            mTimeView.setText(formattedTime);
                        }
                    }));
            eventBus.post(new RequestStatusEvent());
        }
    }

    /**
     * Checks whether stopwatch service running or not
     *
     * @param serviceClass Service class
     * @return True, if stopwatch service is running. False otherwise
     */
    private boolean isStopwatchServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mIsRunning) {
            Intent i = new Intent(this, StopWatchService.class);
            stopService(i);
        }

        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    /**
     * Sends a message to the connected device
     *
     * @param path Message path
     * @param text Message
     */
    private void sendMessage(String path, String text) {
        byte[] data = text != null ? text.getBytes() : null;
        sendMessage(path, data);
    }

    /**
     * Sends a message to the connected device
     *
     * @param path Message path
     * @param data Message data
     */
    private void sendMessage(final String path, final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                        .getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {

                    Wearable.MessageApi
                            .sendMessage(mGoogleApiClient, node.getId(), path, data).await();
                }
            }
        }).start();
    }

    /**
     * Initializes view controls
     */
    private void initializeViews() {
        mTimeView = (TextView) findViewById(R.id.tv_time);

        mLapsListView = (ListView) findViewById(R.id.lv_laps);
        mLapsAdapter = new LapAdapter(this, android.R.layout.simple_list_item_1,
                mLaps);
        mLapsListView.setAdapter(mLapsAdapter);

        // start button
        mBtnStart = (ImageButton) findViewById(R.id.ib_start);
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLocked) {
                    handleStartButton();
                }
            }
        });

        // stop button
        mBtnStop = (ImageButton) findViewById(R.id.ib_stop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLocked) {
                    handleStopButton();
                }
            }
        });

        // lock button
        mBtnLock = (ImageButton) findViewById(R.id.ib_lock);
        mBtnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLockAction();
            }
        });
    }

    /**
     * Handles the action Start
     */
    private void handleStartButton() {

        if (!mIsRunning) {
            mIsRunning = true;

            eventBus.post(new StartStopwatchEvent(
                    new StartStopwatchEvent.StopWatchTickCallback() {
                        @Override
                        public void onTick(long elapsedTime) {

                            String formattedTime = StringUtils.formatString(elapsedTime);
                            mTimeView.setText(formattedTime);
                        }
                    }));

            // Change View state
            mBtnStart.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_playback_pause));
            mBtnStart.setBackgroundColor(getResources().getColor(R.color.orange));
            mBtnStop.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_add));
            mBtnStop.setBackgroundColor(getResources().getColor(R.color.blue));

            // sending connected devices message START
            sendMessage(START_STOPWATCH_PATH, "Stopwatch timer started!");
        } else {

            eventBus.post(new PauseStopwatchEvent(
                    new PauseStopwatchEvent.PauseTimeReceiver() {
                        @Override
                        public void receivePauseTime(long pauseTime) {
                            String formattedTime = StringUtils.formatString(pauseTime);
                            mTimeView.setText(formattedTime);
                        }
                    }));

            mIsRunning = false;

            // Change View state
            mBtnStart.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_playback_play));
            mBtnStart.setBackgroundColor(getResources().getColor(R.color.green));
            mBtnStop.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_reload));
            mBtnStop.setBackgroundColor(getResources().getColor(R.color.dark_blue));
        }
    }

    /**
     * Handles the action Stop
     */
    private void handleStopButton() {
        if (!mIsRunning) {

            // sending connected devices message RESET
            sendMessage(RESET_STOPWATCH, "Reset Stopwatch");

            eventBus.post(new ResetStopWatchEvent());
        } else {
            eventBus.post(new CreateLapEvent());
        }
    }

    /**
     * Handles the action Lock what appropriate button pressed
     */
    private void handleLockAction() {
        if (!mLocked) {
            // Lock
            mBtnLock.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_lock_closed));
            mBtnStop.setBackgroundColor(getResources().getColor(R.color.grey));
            mBtnStart.setBackgroundColor(getResources().getColor(R.color.light_grey));
            mBtnStop.setBackgroundColor(getResources().getColor(R.color.light_grey));
            mLocked = true;
        } else {
            // Unlock
            mBtnLock.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_lock_open));
            if (mIsRunning) {
                mBtnStop.setBackgroundColor(getResources().getColor(R.color.dark_blue));
                mBtnStart.setBackgroundColor(getResources().getColor(R.color.orange));
            } else {
                mBtnStart.setBackgroundColor(getResources().getColor(R.color.green));
                mBtnStop.setBackgroundColor(getResources().getColor(R.color.blue));
            }
            mLocked = false;
        }
    }

    /**
     * Handles LapUpdateEvent
     *
     * @param event Event data
     */
    public void onEvent(LapUpdateEvent event) {
        mLaps = event.getLaps();
        Collections.sort(mLaps, new LapComparator());
        mLapsAdapter = new LapAdapter(this, android.R.layout.simple_list_item_1,
                mLaps);
        mLapsListView.setAdapter(mLapsAdapter);

        // send connected devices message NEW LAP
        byte[] data = SerializeUtils.lapsToByteArray((ArrayList<Lap>) mLaps);
        sendMessage(NEW_LAP_MESSAGE, data);
    }

    /**
     * Handles SaveLapsEvent
     *
     * @param event Event data
     */
    public void onEvent(SaveLapsEvent event) {
        // do nothing
    }

    /**
     * Handles StatusResponseEvent
     *
     * @param event Event data
     */
    public void onEvent(StatusResponseEvent event) {
        Log.d(getClass().getSimpleName(), event.isRunning() ? "running"
                : "not running");
        if (event.isRunning()) {
            // Change View state
            mIsRunning = true;
            mBtnStart.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_playback_pause));
            mBtnStart.setBackgroundColor(getResources().getColor(R.color.orange));
            mBtnStop.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_add));
            mBtnStop.setBackgroundColor(getResources().getColor(R.color.blue));
        } else {
            mIsRunning = false;

            // Change View state
            mBtnStart.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_playback_play));
            mBtnStart.setBackgroundColor(getResources().getColor(R.color.green));
            mBtnStop.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_reload));
            mBtnStop.setBackgroundColor(getResources().getColor(R.color.dark_blue));
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getClass().getSimpleName(), "onConnected: " + bundle);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // do nothing
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        // do nothing
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // do nothing
    }
}
