package com.cologne.hackaton.wearstopwatch.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cologne.hackaton.wearstopwatch.R;
import com.cologne.hackaton.wearstopwatch.model.Lap;
import com.cologne.hackaton.wearstopwatch.utils.StringUtils;
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
import java.util.List;

/**
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class MainActivity extends Activity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private static final String START_STOPWATCH_PATH = "start/stopwatch";
    private static final String SAVE_LAP_MESSAGE = "save/laptime";
    private static final String RESET_STOPWATCH = "reset/watch";
    private static final String PAUSE_WATCH = "pause/watch";
    
    // Views
    private TextView mIimeView;
    private ImageButton mBtnStart;
    private ImageButton mBtnStop;
    private ImageButton mBtnLock;
    private ListView mLapsListView;

    // Statuses
    private boolean mLocked;
    private boolean mRunning;

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
    private LapAdapter mLapsAdapter;
    // Wear Api
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        mUpdaterTask = new Runnable() {
            @Override
            public void run() {
                mTimeInMilliseconds = SystemClock.uptimeMillis() - mStartTime;
                mUpdatedTime = mLastLapTime + mTimeSwapBuffer + mTimeInMilliseconds;

                refreshTimeText();
                mHandler.postDelayed(this, 0);
            }
        };

        initializeViews();

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Initializes view controls
     */
    private void initializeViews() {
        mIimeView = (TextView) findViewById(R.id.tv_time);

        refreshTimeText();
        mLapsListView = (ListView) findViewById(R.id.lv_laps);
        mLapsAdapter = new LapAdapter(this, android.R.layout.simple_list_item_1, mLaps);
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
        if (!mRunning) {
            startStopWatch(0, true);
        } else {
            // Pause
            pauseStopWatch(0, true);
        }
    }

    /**
     * Handles the action Stop
     */
    private void handleStopButton() {
        if (!mRunning) {
            resetStopWatch(true);
        } else {
            saveLap(0, true);
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
            mBtnStart.setBackgroundColor(getResources()
                    .getColor(R.color.light_grey));
            mBtnStop
                    .setBackgroundColor(getResources().getColor(R.color.light_grey));
            mLocked = true;
        } else {
            // Unlock
            mBtnLock.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_lock_open));
            if (mRunning) {
                mBtnStop.setBackgroundColor(getResources()
                        .getColor(R.color.dark_blue));
                mBtnStart.setBackgroundColor(getResources().getColor(R.color.orange));
            } else {
                mBtnStart.setBackgroundColor(getResources().getColor(R.color.green));
                mBtnStop.setBackgroundColor(getResources().getColor(R.color.blue));
            }
            mLocked = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    /**
     * Refreshes the time text
     */
    private void refreshTimeText() {
        String formattedTime = StringUtils.formatString(mUpdatedTime);
        mIimeView.setText(formattedTime);
    }

    /**
     * Does saving the lap time
     *
     * @param lapTime     Lap time
     * @param sendMessage True, if message should be sent
     */
    private void saveLap(long lapTime, boolean sendMessage) {

        if (lapTime == 0) {
            lapTime = mUpdatedTime - mLastLapTime;
        }

        mLaps.add(0, new Lap(lapTime));
        mLastLapTime = mUpdatedTime;
        mLapsAdapter.notifyDataSetChanged();
        mStartTime = SystemClock.uptimeMillis();
        mTimeInMilliseconds = 0L;
        mTimeSwapBuffer = 0L;
        mUpdatedTime = 0L;
        if (sendMessage) {
            sendMessage(SAVE_LAP_MESSAGE, Long.toString(lapTime));
        }
    }

    /**
     * Resets stop watch timer
     *
     * @param sendMessage True, if message should be sent
     */
    private void resetStopWatch(boolean sendMessage) {
        mLastLapTime = 0L;
        mStartTime = 0L;
        mTimeInMilliseconds = 0L;
        mTimeSwapBuffer = 0L;
        mUpdatedTime = 0L;
        mLapsAdapter.clear();
        refreshTimeText();
        if (sendMessage) {
            sendMessage(RESET_STOPWATCH, null);
        }
    }

    /**
     * Pauses stopwatch timer
     *
     * @param timeSwapBufferParam
     * @param sendMessage True, if message should be sent
     */
    private void pauseStopWatch(long timeSwapBufferParam, boolean sendMessage) {

        if (timeSwapBufferParam == 0) {
            mTimeSwapBuffer += mTimeInMilliseconds;
        } else {
            mTimeSwapBuffer = timeSwapBufferParam;
        }

        mHandler.removeCallbacks(mUpdaterTask);

        mBtnStart.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_playback_play));
        mBtnStart.setBackgroundColor(getResources().getColor(R.color.green));
        mBtnStop.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_reload));
        mBtnStop.setBackgroundColor(getResources().getColor(R.color.dark_blue));
        mRunning = false;

        if (sendMessage) {
            sendMessage(PAUSE_WATCH, Long.toString(mTimeSwapBuffer));
        }
    }

    /**
     * Launches stop watch timer
     *
     * @param startTimeParam Start time
     * @param sendMessage    True, if message should be sent
     */
    private void startStopWatch(long startTimeParam, boolean sendMessage) {
        if (startTimeParam == 0) {
            mStartTime = SystemClock.uptimeMillis();
        } else {
            mStartTime = startTimeParam;
        }

        mHandler.post(mUpdaterTask);

        // Start
        mBtnStart.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_playback_pause));
        mBtnStart.setBackgroundColor(getResources().getColor(R.color.orange));
        mBtnStop.setImageDrawable(getResources().getDrawable(R.mipmap.ic_action_add));
        mBtnStop.setBackgroundColor(getResources().getColor(R.color.blue));
        mRunning = true;

        if (sendMessage) {
            sendMessage(START_STOPWATCH_PATH, Long.toString(mStartTime));
        }
    }

    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                        .getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi
                            .sendMessage(mGoogleApiClient, node.getId(), path,
                                    text != null ? text.getBytes() : null).await();
                }
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(getClass().getSimpleName(), "onConnected: " + connectionHint);
        Toast.makeText(MainActivity.this, "Connected to Data Layer API",
                Toast.LENGTH_SHORT).show();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(getClass().getSimpleName(), "onConnectionSuspended: " + cause);
        Toast.makeText(MainActivity.this, "Connection to Data ayer API suspended",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(getClass().getSimpleName(), "onConnectionFailed: " + connectionResult);
        Toast.makeText(MainActivity.this, "Connection to Data Layer API failed",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d(messageEvent.getPath(), "message received");
        if (messageEvent.getPath().equals(START_STOPWATCH_PATH)) {
            mStartTime = Long.valueOf(new String(messageEvent.getData()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startStopWatch(mStartTime, false);
                }
            });
        } else if (messageEvent.getPath().equals(PAUSE_WATCH)) {
            mTimeSwapBuffer = Long.valueOf(new String(messageEvent.getData()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pauseStopWatch(mTimeSwapBuffer, false);
                }
            });

        } else if (messageEvent.getPath().equals(RESET_STOPWATCH)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resetStopWatch(false);
                }
            });
        } else if (messageEvent.getPath().equals(SAVE_LAP_MESSAGE)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long lapTime = Long.valueOf(new String(messageEvent.getData()));
                    saveLap(lapTime, false);
                }
            });
        }
    }


}
