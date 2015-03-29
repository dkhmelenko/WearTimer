package com.cologne.hackaton.wearstopwatch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cologne.hackaton.wearstopwatch.timelib.model.Lap;
import com.cologne.hackaton.wearstopwatch.timelib.utils.SerializeUtils;
import com.cologne.hackaton.wearstopwatch.timelib.utils.StringUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application activity
 *
 * @author Dmytro Khmelenko
 */
public class MainActivity extends Activity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private static final String START_STOPWATCH_PATH = "start/stopwatch";
    private static final String SAVE_LAP_MESSAGE = "save/laptime";
    private static final String RESET_STOPWATCH = "reset/watch";

    // Laps
    private List<Lap> mLaps = new ArrayList<>();
    private ListView lapsListView;
    private LapAdapter mLapsAdapter;

    // Wear Api
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .build();
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
     * Initializes view controls
     */
    private void initializeViews() {
        lapsListView = (ListView) findViewById(R.id.lv_laps);
        mLapsAdapter = new LapAdapter(this, android.R.layout.simple_list_item_1,
                mLaps);
        lapsListView.setAdapter(mLapsAdapter);

        TextView emptyView = (TextView) findViewById(R.id.empty_laps_view);
        lapsListView.setEmptyView(emptyView);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getClass().getSimpleName(), "onConnected: " + bundle);
        Toast.makeText(MainActivity.this, "Connected with watch",
                Toast.LENGTH_SHORT).show();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(getClass().getSimpleName(), "onConnectionSuspended: " + i);
        Toast.makeText(MainActivity.this, "Connection with watch suspended",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(getClass().getSimpleName(), "onConnectionFailed: " + connectionResult);
        Toast.makeText(MainActivity.this, "Connection with Watch failed",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        // do nothing
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d(messageEvent.getPath(), "message received");
        if (messageEvent.getPath().equals(START_STOPWATCH_PATH)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleStartStopwatch();
                }
            });
        } else if (messageEvent.getPath().equals(RESET_STOPWATCH)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleResetStopwatch();
                }
            });
        } else if (messageEvent.getPath().equals(SAVE_LAP_MESSAGE)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleLapAdded(messageEvent);
                }
            });
        }
    }

    /**
     * Handles event Lap added
     *
     * @param messageEvent Event data
     */
    private void handleLapAdded(MessageEvent messageEvent) {
        ArrayList<Lap> newLaps = SerializeUtils.byteArrayToLaps(messageEvent.getData());

        if(newLaps != null) {
            mLaps.clear();
            mLaps.addAll(newLaps);
            mLapsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Handles event Start stopwatch
     */
    private void handleStartStopwatch() {
        // do nothing
    }

    /**
     * Handles event Reset stopwatch
     */
    private void handleResetStopwatch() {
        mLaps.clear();
        mLapsAdapter.notifyDataSetChanged();
    }

}
