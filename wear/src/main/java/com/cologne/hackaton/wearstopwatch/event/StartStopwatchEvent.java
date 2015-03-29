package com.cologne.hackaton.wearstopwatch.event;

/**
 * Created by gabriel on 3/5/2015.
 */
public class StartStopwatchEvent {

  private StopWatchTickCallback callback;

  public StartStopwatchEvent(StopWatchTickCallback callback) {
    this.callback = callback;
  }

  public StopWatchTickCallback getCallback() {
    return callback;
  }

  public interface StopWatchTickCallback {
    public void onTick(long elapsedTime);
  }
}
