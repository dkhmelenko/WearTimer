package com.cologne.hackaton.wearstopwatch.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.RelativeLayout;

import com.cologne.hackaton.wearstopwatch.R;

/**
 * Created by admin on 3/4/2015.
 */
public class StartActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_start);

    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        initializeViews();
      }
    });
  }

  private void initializeViews() {
    RelativeLayout rlStopWatch = (RelativeLayout) findViewById(R.id.rl_stopwatch);
    rlStopWatch.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startStopWatch();
      }
    });
    RelativeLayout rlTimer = (RelativeLayout) findViewById(R.id.rl_timer);
    rlTimer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startTimer();
      }
    });
    RelativeLayout rlAlarm = (RelativeLayout) findViewById(R.id.rl_alarm);
    rlAlarm.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startAlarms();
      }
    });
    RelativeLayout rlWorlsClock = (RelativeLayout) findViewById(R.id.rl_worldclock);
    rlWorlsClock.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startWorldClock();
      }
    });
  }

  private void startWorldClock() {

  }

  private void startAlarms() {

  }

  private void startTimer() {
    Intent timerIntent = new Intent(this, TimerActivity.class);
    startActivity(timerIntent);
  }

  private void startStopWatch() {
    Intent stopWatchIntent = new Intent(this, StopWatchActivity.class);
    startActivity(stopWatchIntent);
  }
}
